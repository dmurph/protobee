package org.protobee.network;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.protobee.annotation.InjectLogger;
import org.protobee.identity.NetworkIdentity;
import org.protobee.identity.NetworkIdentityManager;
import org.protobee.network.handlers.MultipleRequestReceiver;
import org.protobee.network.handlers.SingleRequestReceiver;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.ProtocolConfig;
import org.protobee.util.ProtocolConfigUtils;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;


@Singleton
public class ConnectionBinderImpl implements ConnectionBinder {

  @InjectLogger
  private Logger log;
  private final NetworkIdentityManager identityManager;
  private final Provider<ServerBootstrap> bootstrapProvider;
  private final MultipleRequestReceiver.Factory requestMultiplexerFactory;
  private final SingleRequestReceiver.Factory requestHandlerFactory;
  private final ProtobeeChannels channels;

  @Inject
  public ConnectionBinderImpl(NetworkIdentityManager identityManager,
      Provider<ServerBootstrap> bootstrapProvider, MultipleRequestReceiver.Factory multiplexer,
      SingleRequestReceiver.Factory requestHandlerFactory, ProtobeeChannels channels) {
    this.identityManager = identityManager;
    this.bootstrapProvider = bootstrapProvider;
    this.requestMultiplexerFactory = multiplexer;
    this.requestHandlerFactory = requestHandlerFactory;
    this.channels = channels;
  }

  @Override
  public Channel bind(final ProtocolConfig config) {
    Preconditions.checkNotNull(config);

    ChannelPipelineFactory factory = new ChannelPipelineFactory() {
      @Override
      public ChannelPipeline getPipeline() throws Exception {
        SingleRequestReceiver handler = requestHandlerFactory.create(config);
        return Channels.pipeline(handler);
      }
    };

    ServerBootstrap bootstrap = bootstrapProvider.get();
    bootstrap.setOptions(config.getNettyBootstrapOptions());
    bootstrap.setPipelineFactory(factory);

    // yes, this means our address will be a localhost address for a little bit. This should change
    // when we get the 'Remote-IP' header for the first time
    int port = config.getPort();
    Protocol protocol = config.get();
    NetworkIdentity me = identityManager.getMe();

    InetSocketAddress localAddress = (InetSocketAddress) me.getListeningAddress(protocol);
    if (localAddress == null) {
      localAddress = new InetSocketAddress(port);
    } else {
      localAddress = new InetSocketAddress(localAddress.getAddress(), port);
    }
    identityManager.setListeningAddress(me, protocol, localAddress);

    Channel channel = bootstrap.bind(localAddress);
    log.info("Port " + config.getPort() + " bound for protocol " + config);
    channels.addChannel(channel, config.get());
    return channel;
  }

  @Override
  public Channel bind(final Set<ProtocolConfig> configs, int port) {
    Preconditions.checkNotNull(configs);
    Preconditions.checkArgument(port > 0, "port must be > 0");

    Map<String, Object> options = ProtocolConfigUtils.mergeNettyBindOptions(configs);

    ChannelPipelineFactory factory = new ChannelPipelineFactory() {
      @Override
      public ChannelPipeline getPipeline() throws Exception {
        MultipleRequestReceiver handler = requestMultiplexerFactory.create(configs);
        return Channels.pipeline(handler);
      }
    };

    ServerBootstrap bootstrap = bootstrapProvider.get();
    bootstrap.setOptions(options);
    bootstrap.setPipelineFactory(factory);

    // yes, this means our address might be a localhost address for a little bit. This should be
    // changed
    // by the protocol when it knows our address
    for (ProtocolConfig protocolConfig : configs) {
      Protocol protocol = protocolConfig.get();
      NetworkIdentity me = identityManager.getMe();

      InetSocketAddress localAddress = (InetSocketAddress) me.getListeningAddress(protocol);
      if (localAddress == null) {
        localAddress = new InetSocketAddress(port);
      } else {
        localAddress = new InetSocketAddress(localAddress.getAddress(), port);
      }
      identityManager.setListeningAddress(me, protocolConfig.get(), localAddress);
    }
    Channel channel = bootstrap.bind(new InetSocketAddress(port));
    channels.addChannel(channel, ProtocolConfigUtils.getProtocolSet(configs));

    log.info("Port " + port + " bound for protocols " + configs);
    return channel;
  }
}
