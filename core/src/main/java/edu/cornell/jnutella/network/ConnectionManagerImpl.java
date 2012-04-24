package edu.cornell.jnutella.network;

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
import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.identity.NetworkIdentityManager;
import edu.cornell.jnutella.protocol.HandshakeFuture;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.protocol.ProtocolConfig;
import edu.cornell.jnutella.session.HandshakeCreator;
import edu.cornell.jnutella.session.SessionModel;

@Singleton
public class ConnectionManagerImpl implements ConnectionCreator {

  @InjectLogger
  private Logger log;
  private final Map<Protocol, ProtocolConfig> protocolConfigs;
  private final Provider<HandshakeCreator> handshakeCreator;
  private final NetworkIdentityManager identityManager;
  private final HandshakeStateBootstrapper handshakeBootstrapper;
  private final ChannelFactory channelFactory;
  private final Provider<Channel> channelProvider;
  private final Object connectLock = new Object();
  private final JnutellaChannels channels;

  @Inject
  public ConnectionManagerImpl(Map<Protocol, ProtocolConfig> protocolConfigs,
      Provider<HandshakeCreator> handshakeCreator, NetworkIdentityManager identityManager,
      HandshakeStateBootstrapper handshakeBootstrapper, ChannelFactory channelFactory,
      Provider<Channel> channelProvider, JnutellaChannels channels) {
    this.protocolConfigs = protocolConfigs;
    this.handshakeCreator = handshakeCreator;
    this.identityManager = identityManager;
    this.handshakeBootstrapper = handshakeBootstrapper;
    this.channelFactory = channelFactory;
    this.channelProvider = channelProvider;
    this.channels = channels;
  }

  @Override
  public ChannelFuture connect(final Protocol protocol, final SocketAddress remoteAddress) {
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
          handshakeBootstrapper.bootstrapSession(config, identity, remoteAddress, null, pipeline);
          return pipeline;
        }
      };
      ClientBootstrap bootstrap = new ClientBootstrap(channelFactory);
      bootstrap.setOptions(config.getNettyBootstrapOptions());
      bootstrap.setPipelineFactory(factory);
      ChannelFuture future = bootstrap.connect(remoteAddress);

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
            request = handshake.createHandshakeRequest();
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
      SessionModel session = identity.getCurrentSession(protocol);
      ChannelFuture future;
      try {
        identity.enterScope();
        session.enterScope();
        Channel channel = channelProvider.get();
        log.info("Disconnecting channel " + channel + " of protocol " + protocol + " at address "
            + remoteAddress);
        future = channel.disconnect();
        channels.removeChannel(channel, protocol);

        future.addListener(new ChannelFutureListener() {
          @Override
          public void operationComplete(ChannelFuture future) throws Exception {
            identity.clearCurrentSession(protocol);
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
