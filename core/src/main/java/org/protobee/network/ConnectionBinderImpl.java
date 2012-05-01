package org.protobee.network;

import java.net.SocketAddress;
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
    bootstrap.setOptions(config.getServerBootstrapOptions());
    bootstrap.setPipelineFactory(factory);

    SocketAddress localAddress = config.getListeningAddress();
    Protocol protocol = config.get();
    NetworkIdentity me = identityManager.getMe();
    identityManager.setListeningAddress(me, protocol, localAddress);

    Channel channel = bootstrap.bind(localAddress);
    log.info("Address " + localAddress + " bound for protocol " + config);
    channels.addChannel(channel, config.get());
    return channel;
  }

  @Override
  public Channel bind(final Set<ProtocolConfig> configs, SocketAddress address) {
    Preconditions.checkNotNull(configs);
    Preconditions.checkNotNull(address);

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

    for (ProtocolConfig protocolConfig : configs) {
      Protocol protocol = protocolConfig.get();
      NetworkIdentity me = identityManager.getMe();
      Preconditions.checkArgument(address.equals(protocolConfig.getListeningAddress()),
          "Listening addresses do not match.");
      identityManager.setListeningAddress(me, protocol, address);
    }
    Channel channel = bootstrap.bind(address);
    channels.addChannel(channel, ProtocolConfigUtils.getProtocolSet(configs));

    log.info("Address " + address + " bound for protocols " + configs);
    return channel;
  }
}
