package org.protobee.network;

import java.net.SocketAddress;
import java.util.Map;

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
import org.protobee.protocol.ProtocolConfig;
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
  private final Map<Protocol, ProtocolConfig> protocolConfigs;
  private final Provider<HandshakeCreator> handshakeCreator;
  private final NetworkIdentityManager identityManager;
  private final HandshakeStateBootstrapper handshakeBootstrapper;
  private final ChannelFactory channelFactory;
  private final Provider<Channel> channelProvider;
  private final Object connectLock = new Object();
  private final ProtobeeChannels channels;

  @Inject
  public ConnectionCreatorImpl(Map<Protocol, ProtocolConfig> protocolConfigs,
      Provider<HandshakeCreator> handshakeCreator, NetworkIdentityManager identityManager,
      HandshakeStateBootstrapper handshakeBootstrapper, ChannelFactory channelFactory,
      Provider<Channel> channelProvider, ProtobeeChannels channels) {
    this.protocolConfigs = protocolConfigs;
    this.handshakeCreator = handshakeCreator;
    this.identityManager = identityManager;
    this.handshakeBootstrapper = handshakeBootstrapper;
    this.channelFactory = channelFactory;
    this.channelProvider = channelProvider;
    this.channels = channels;
  }

  @Override
  public ChannelFuture connect(final Protocol protocol, final SocketAddress remoteAddress,
      final HttpMethod method, final String uri) {
    Preconditions.checkNotNull(protocol);
    Preconditions.checkNotNull(remoteAddress);
    Preconditions.checkArgument(protocolConfigs.containsKey(protocol),
        "Protocol specified does not come from a protocol config");
    final ProtocolConfig config = protocolConfigs.get(protocol);
    synchronized (connectLock) {
      final NetworkIdentity identity =
          identityManager.getNetworkIdentityWithNewConnection(protocol, remoteAddress);
      ChannelPipelineFactory factory = new ChannelPipelineFactory() {
        @Override
        public ChannelPipeline getPipeline() throws Exception {
          ChannelPipeline pipeline = Channels.pipeline();
          handshakeBootstrapper.bootstrapSession(config, identity, null, pipeline);
          return pipeline;
        }
      };
      ClientBootstrap bootstrap = new ClientBootstrap(channelFactory);
      bootstrap.setOptions(config.getConnectionOptions());
      bootstrap.setPipelineFactory(factory);
      ChannelFuture future = bootstrap.connect(config.getListeningAddress(), remoteAddress);

      channels.addChannel(future.getChannel(), protocol);

      final ChannelFuture handshakeFinishedFuture =
          new DefaultChannelFuture(future.getChannel(), false);
      future.addListener(new ChannelFutureListener() {

        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
          if (!future.isSuccess()) {
            log.error("Could not connect to " + remoteAddress + " for '" + protocol + "' protocol",
                future.getCause());

          }
          log.info("Connected to " + remoteAddress + " for '" + protocol + "' protocol");

          // put the channel on the session scope
          SessionModel model = identity.getCurrentSession(protocol);
          model.addObjectToScope(Key.get(Channel.class), future.getChannel());
          model.addObjectToScope(Key.get(ChannelFuture.class, HandshakeFuture.class),
              handshakeFinishedFuture);

          // send our handshake
          HttpMessage request;
          try {
            identity.enterScope();
            model.enterScope();
            HandshakeCreator handshake = handshakeCreator.get();
            request = handshake.createHandshakeRequest(method, uri);
          } finally {
            model.exitScope();
            identity.exitScope();
          }

          Channels.write(future.getChannel(), request);
        }
      });
      return handshakeFinishedFuture;
    }
  }

  @Override
  public ChannelFuture disconnect(final Protocol protocol, SocketAddress remoteAddress) {
    Preconditions.checkNotNull(protocol);
    Preconditions.checkNotNull(remoteAddress);
    final NetworkIdentity identity = identityManager.getNewtorkIdentity(remoteAddress);

    synchronized (connectLock) {
      if (!identity.hasCurrentSession(protocol)) {
        log.info("We don't have a current session for protocol " + protocol + " with identity "
            + identity + " at address " + remoteAddress);
        ChannelFuture future = new DefaultChannelFuture(null, false);
        future.setSuccess();
        return future;
      }
      final SessionModel session = identity.getCurrentSession(protocol);
      ChannelFuture future;
      try {
        identity.enterScope();
        session.enterScope();
        Channel channel = channelProvider.get();
        log.info("Disconnecting channel " + channel + " of protocol " + protocol + " at address "
            + remoteAddress);
        future = channel.close();
        channels.removeChannel(channel, protocol);

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
        identity.exitScope();
      }
      return future;
    }
  }
}
