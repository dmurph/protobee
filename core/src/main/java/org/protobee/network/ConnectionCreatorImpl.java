package org.protobee.network;

import java.net.SocketAddress;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.DefaultChannelFuture;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.protobee.annotation.InjectLogger;
import org.protobee.identity.NetworkIdentity;
import org.protobee.identity.NetworkIdentityManager;
import org.protobee.protocol.HandshakeFuture;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.ProtocolModel;
import org.protobee.session.SessionModel;
import org.protobee.session.handshake.HandshakeCreator;
import org.protobee.session.handshake.HandshakeStateBootstrapper;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Singleton;


@Singleton
public class ConnectionCreatorImpl implements ConnectionCreator {

  @InjectLogger
  private Logger log;
  private final Provider<HandshakeCreator> handshakeCreator;
  private final NetworkIdentityManager identityManager;
  private final HandshakeStateBootstrapper handshakeBootstrapper;
  private final ChannelFactory channelFactory;
  private final Provider<Channel> channelProvider;
  private final Object connectLock = new Object();
  private final ProtobeeChannels channels;

  @Inject
  public ConnectionCreatorImpl(Provider<HandshakeCreator> handshakeCreator,
      NetworkIdentityManager identityManager, HandshakeStateBootstrapper handshakeBootstrapper,
      ChannelFactory channelFactory, Provider<Channel> channelProvider, ProtobeeChannels channels) {
    this.handshakeCreator = handshakeCreator;
    this.identityManager = identityManager;
    this.handshakeBootstrapper = handshakeBootstrapper;
    this.channelFactory = channelFactory;
    this.channelProvider = channelProvider;
    this.channels = channels;
  }

  @Override
  public ChannelFuture connect(final ProtocolModel protocolModel,
      final SocketAddress remoteAddress, final HttpMethod method, final String uri) {
    Preconditions.checkNotNull(protocolModel);
    Preconditions.checkNotNull(remoteAddress);

    final Protocol protocol = protocolModel.getProtocol();

    synchronized (connectLock) {
      final NetworkIdentity identity =
          identityManager.getNetworkIdentityWithNewConnection(protocol, remoteAddress);
      ChannelPipelineFactory factory = new ChannelPipelineFactory() {
        @Override
        public ChannelPipeline getPipeline() throws Exception {
          ChannelPipeline pipeline = Channels.pipeline();
          try {
            protocolModel.enterScope();
            handshakeBootstrapper.bootstrapSession(protocolModel, identity, null, pipeline);
          } finally {
            protocolModel.exitScope();
          }
          return pipeline;
        }
      };


      ClientBootstrap bootstrap = new ClientBootstrap(channelFactory);
      SocketAddress listeningAddress = protocolModel.getLocalListeningAddress();
      bootstrap.setOptions(protocolModel.getConnectionOptions());
      bootstrap.setPipelineFactory(factory);
      ChannelFuture future = bootstrap.connect(listeningAddress, remoteAddress);

      channels.addChannel(future.getChannel(), protocol);

      final ChannelFuture handshakeFinishedFuture =
          new DefaultChannelFuture(future.getChannel(), false);
      future.addListener(new ChannelFutureListener() {

        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
          if (!future.isSuccess()) {
            log.error("Could not connect to " + remoteAddress + " for '" + protocolModel
                + "' protocol", future.getCause());

          }
          log.info("Connected to " + remoteAddress + " for '" + protocolModel + "' protocol");

          // put the channel on the session scope
          SessionModel model = identity.getCurrentSession(protocol);
          model.getScope().putInScope(Key.get(Channel.class), future.getChannel());
          model.getScope().putInScope(Key.get(ChannelFuture.class, HandshakeFuture.class),
              handshakeFinishedFuture);

          // send our handshake
          HttpMessage request;
          try {
            protocolModel.enterScope();
            HandshakeCreator handshake = handshakeCreator.get();
            request = handshake.createHandshakeRequest(protocol, method, uri);
          } finally {
            protocolModel.exitScope();
          }

          Channels.write(future.getChannel(), request);
        }
      });
      return handshakeFinishedFuture;
    }
  }

  @Override
  public ChannelFuture disconnect(final ProtocolModel protocolModel, SocketAddress remoteAddress) {
    Preconditions.checkNotNull(protocolModel);
    Preconditions.checkNotNull(remoteAddress);
    final NetworkIdentity identity = identityManager.getNewtorkIdentity(remoteAddress);

    final Protocol protocol = protocolModel.getProtocol();

    synchronized (connectLock) {
      if (!identity.hasCurrentSession(protocol)) {
        log.info("We don't have a current session for protocol " + protocolModel
            + " with identity " + identity + " at address " + remoteAddress);
        ChannelFuture future = new DefaultChannelFuture(null, false);
        future.setSuccess();
        return future;
      }
      final SessionModel session = identity.getCurrentSession(protocol);
      ChannelFuture future;
      try {
        session.enterScope();
        Channel channel = channelProvider.get();
        log.info("Disconnecting channel " + channel + " of protocol " + protocolModel
            + " at address " + remoteAddress);
        future = channel.close();
        // this is automatic with channel groups:
        // channels.removeChannel(channel, protocol);

        // this should be taken care of by the cleanup handler, people might not include it :/
        future.addListener(new ChannelFutureListener() {
          @Override
          public void operationComplete(ChannelFuture future) throws Exception {
            if (identity.getCurrentSession(protocol) == session) {
              identity.clearCurrentSession(protocol);
            }
          }
        });
      } finally {
        session.exitScope();
      }
      return future;
    }
  }
}
