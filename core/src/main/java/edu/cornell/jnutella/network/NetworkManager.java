package edu.cornell.jnutella.network;

import java.net.SocketAddress;
import java.util.Map;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
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
import edu.cornell.jnutella.identity.ProtocolIdentityModel;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.protocol.ProtocolConfig;
import edu.cornell.jnutella.protocol.session.HandshakeCreator;
import edu.cornell.jnutella.protocol.session.SessionModel;

@Singleton
public class NetworkManager {

  @InjectLogger
  private Logger log;
  private final Map<Protocol, ProtocolConfig> protocolConfigs;
  private final Provider<HandshakeCreator> handshakeCreator;
  private final NetworkIdentityManager identityManager;
  private final HandshakeStateBootstrapper handshakeBootstrapper;
  private final ChannelFactory channelFactory;
  private final ReceivingRequestMultiplexer multiplexer;
  private final Provider<Channel> channelProvider;

  @Inject
  public NetworkManager(Map<Protocol, ProtocolConfig> protocolConfigs,
      Provider<HandshakeCreator> handshakeCreator, NetworkIdentityManager identityManager,
      HandshakeStateBootstrapper handshakeBootstrapper, ChannelFactory channelFactory,
      ReceivingRequestMultiplexer multiplexer, Provider<Channel> channelProvider) {
    this.protocolConfigs = protocolConfigs;
    this.handshakeCreator = handshakeCreator;
    this.identityManager = identityManager;
    this.handshakeBootstrapper = handshakeBootstrapper;
    this.channelFactory = channelFactory;
    this.multiplexer = multiplexer;
    this.channelProvider = channelProvider;
  }

  public ChannelFuture connect(final Protocol protocol, final SocketAddress remoteAddress) {
    Preconditions.checkNotNull(protocol);
    Preconditions.checkNotNull(remoteAddress);
    Preconditions.checkArgument(protocolConfigs.containsKey(protocol),
        "Protocol specified does not come from a protocol config");
    final ProtocolConfig config = protocolConfigs.get(protocol);
    final NetworkIdentity identity =
        identityManager.getNetworkIdentityWithNewConnection(protocol, remoteAddress);

    Preconditions.checkState(!identity.hasCurrentSession(protocol),
        "Session already active with that identity.");

    ChannelPipelineFactory factory = new ChannelPipelineFactory() {
      @Override
      public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();
        identity.enterScope();
        ProtocolIdentityModel identityModel = identity.getModel(protocol);
        ChannelHandler[] handlers =
            handshakeBootstrapper.bootstrapSession(config, identityModel, remoteAddress, null);
        identity.exitScope();
        for (ChannelHandler channelHandler : handlers) {
          pipeline.addLast(channelHandler.toString(), channelHandler);
        }
        return pipeline;
      }
    };

    ClientBootstrap bootstrap = new ClientBootstrap(channelFactory);
    bootstrap.setOptions(config.getNettyBootstrapOptions());

    bootstrap.setPipelineFactory(factory);

    ChannelFuture future = bootstrap.connect(remoteAddress);

    // put the channel on the session scope
    SessionModel model = identity.getCurrentSession(protocol);
    model.addObjectToScope(Key.get(Channel.class), future.getChannel());

    // send our handshake
    identity.enterScope();
    model.enterScope();
    HandshakeCreator handshake = handshakeCreator.get();
    HttpMessage request = handshake.createHandshakeRequest();
    model.exitScope();
    identity.exitScope();

    return Channels.write(future.getChannel(), request);
  }

  public Channel bind(SocketAddress localAddress, Map<String, Object> serverOptions) {
    Preconditions.checkNotNull(localAddress);

    ChannelPipelineFactory factory = new ChannelPipelineFactory() {
      @Override
      public ChannelPipeline getPipeline() throws Exception {
        return Channels.pipeline(multiplexer);
      }
    };

    ServerBootstrap bootstrap = new ServerBootstrap(channelFactory);
    bootstrap.setOptions(serverOptions);
    bootstrap.setPipelineFactory(factory);

    return bootstrap.bind(localAddress);
  }

  public ChannelFuture disconnect(final Protocol protocol, SocketAddress remoteAddress) {
    Preconditions.checkNotNull(protocol);
    Preconditions.checkNotNull(remoteAddress);
    final NetworkIdentity identity = identityManager.getNewtorkIdentity(remoteAddress);

    Preconditions.checkState(identity.hasCurrentSession(protocol), "No current session");

    SessionModel session = identity.getCurrentSession(protocol);
    identity.enterScope();
    session.enterScope();
    Channel channel = channelProvider.get();
    ChannelFuture future = channel.disconnect();
    future.addListener(new ChannelFutureListener() {
      @Override
      public void operationComplete(ChannelFuture future) throws Exception {
        identity.clearCurrentSession(protocol);
      }
    });
    session.exitScope();
    identity.exitScope();

    return future;
  }
}
