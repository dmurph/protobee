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
import org.protobee.protocol.LocalListeningAddress;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.ProtocolModel;
import org.protobee.protocol.ServerOptionsMap;
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
  private final Provider<SingleRequestReceiver> requestHandlerFactory;
  private final ProtobeeChannels channels;

  private final Provider<Map<String, Object>> serverOptionsProvider;
  private final Provider<SocketAddress> localAddressProvider;

  @Inject
  public ConnectionBinderImpl(NetworkIdentityManager identityManager,
      Provider<ServerBootstrap> bootstrapProvider, MultipleRequestReceiver.Factory multiplexer,
      Provider<SingleRequestReceiver> requestHandlerFactory, ProtobeeChannels channels,
      @ServerOptionsMap Provider<Map<String, Object>> serverOptionsProvider,
      @LocalListeningAddress Provider<SocketAddress> localAddressProvider) {
    this.identityManager = identityManager;
    this.bootstrapProvider = bootstrapProvider;
    this.requestMultiplexerFactory = multiplexer;
    this.requestHandlerFactory = requestHandlerFactory;
    this.channels = channels;
    this.serverOptionsProvider = serverOptionsProvider;
    this.localAddressProvider = localAddressProvider;
  }

  @Override
  public Channel bind(final ProtocolModel model) {
    Preconditions.checkNotNull(model);

    Protocol protocol = model.getProtocol();

    ChannelPipelineFactory factory = new ChannelPipelineFactory() {
      @Override
      public ChannelPipeline getPipeline() throws Exception {
        SingleRequestReceiver handler;
        try {
          model.enterScope();
          handler = requestHandlerFactory.get();
        } finally {
          model.exitScope();
        }
        return Channels.pipeline(handler);
      }
    };

    SocketAddress localAddress;
    ServerBootstrap bootstrap = bootstrapProvider.get();
    try {
      model.enterScope();
      bootstrap.setOptions(serverOptionsProvider.get());
      bootstrap.setPipelineFactory(factory);

      localAddress = localAddressProvider.get();
      NetworkIdentity me = identityManager.getMe();
      identityManager.setListeningAddress(me, protocol, localAddress);
    } finally {
      model.exitScope();
    }

    Channel channel = bootstrap.bind(localAddress);
    log.info("Address " + localAddress + " bound for protocol " + model.getProtocol());
    channels.addChannel(channel, protocol);

    return channel;
  }

  @Override
  public Channel bind(final Set<ProtocolModel> models, SocketAddress address) {
    Preconditions.checkNotNull(models);
    Preconditions.checkNotNull(address);

    Map<String, Object> options =
        ProtocolConfigUtils.mergeNettyBindOptions(models, serverOptionsProvider);

    ChannelPipelineFactory factory = new ChannelPipelineFactory() {
      @Override
      public ChannelPipeline getPipeline() throws Exception {
        MultipleRequestReceiver handler = requestMultiplexerFactory.create(models);
        return Channels.pipeline(handler);
      }
    };

    ServerBootstrap bootstrap = bootstrapProvider.get();
    bootstrap.setOptions(options);
    bootstrap.setPipelineFactory(factory);

    for (ProtocolModel model : models) {
      Protocol protocol = model.getProtocol();
      NetworkIdentity me = identityManager.getMe();
      try {
        model.enterScope();
        SocketAddress listeningAddress = localAddressProvider.get();
        Preconditions.checkArgument(address.equals(listeningAddress),
            "Listening addresses do not match.");
      } finally {
        model.exitScope();
      }
      identityManager.setListeningAddress(me, protocol, address);
    }
    Channel channel = bootstrap.bind(address);
    channels.addChannel(channel, ProtocolConfigUtils.getProtocolSetFromModels(models));

    log.info("Address " + address + " bound for protocols " + models);
    return channel;
  }
}
