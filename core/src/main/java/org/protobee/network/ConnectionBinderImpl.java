package org.protobee.network;

import java.net.SocketAddress;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.protobee.annotation.InjectLogger;
import org.protobee.identity.NetworkIdentity;
import org.protobee.identity.NetworkIdentityManager;
import org.protobee.network.handlers.MultipleRequestReceiver;
import org.protobee.network.handlers.SingleRequestReceiver;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.ProtocolModel;
import org.protobee.util.ProtocolConfigUtils;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;


@Singleton
public class ConnectionBinderImpl implements ConnectionBinder {

  @InjectLogger
  private Logger log;
  private final NetworkIdentityManager identityManager;
  private final MultipleRequestReceiver.Factory requestMultiplexerFactory;
  private final Provider<SingleRequestReceiver> requestHandlerFactory;
  private final ProtobeeChannels channels;
  private final Provider<ServerBootstrap> bootstrapProvider;

  @Inject
  public ConnectionBinderImpl(NetworkIdentityManager identityManager,
      MultipleRequestReceiver.Factory multiplexer,
      Provider<SingleRequestReceiver> requestHandlerFactory, ProtobeeChannels channels,
      Provider<ServerBootstrap> bootstrapProvider) {
    this.identityManager = identityManager;
    this.requestMultiplexerFactory = multiplexer;
    this.requestHandlerFactory = requestHandlerFactory;
    this.channels = channels;
    this.bootstrapProvider = bootstrapProvider;
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
    ServerBootstrap bootstrap;
    try {
      model.enterScope();
      bootstrap = bootstrapProvider.get();
    } finally {
      model.exitScope();
    }
    bootstrap.setOptions(model.getServerOptions());
    bootstrap.setPipelineFactory(factory);

    localAddress = model.getLocalListeningAddress();
    NetworkIdentity me = identityManager.getMe();
    identityManager.setListeningAddress(me, protocol, localAddress);

    Channel channel = bootstrap.bind(localAddress);
    log.info("Address " + localAddress + " bound for protocol " + model);
    channels.addChannel(channel, protocol);

    return channel;
  }

  @Override
  public Channel bind(final Set<ProtocolModel> models, SocketAddress address) {
    Preconditions.checkNotNull(models);
    Preconditions.checkNotNull(address);

    Map<String, Object> options = ProtocolConfigUtils.mergeNettyBindOptions(models);

    ChannelPipelineFactory factory = new ChannelPipelineFactory() {
      @Override
      public ChannelPipeline getPipeline() throws Exception {
        MultipleRequestReceiver handler = requestMultiplexerFactory.create(models);
        return Channels.pipeline(handler);
      }
    };

    ChannelFactory serverChannelFactory = null;
    for (ProtocolModel model : models) {
      Protocol protocol = model.getProtocol();
      NetworkIdentity me = identityManager.getMe();
      if (serverChannelFactory == null) {
        serverChannelFactory = model.getServerFactory();
      } else {
        Preconditions.checkArgument(serverChannelFactory == model.getServerFactory(),
            "Server factories are not the same.");
      }
      SocketAddress listeningAddress = model.getLocalListeningAddress();
      Preconditions.checkArgument(address.equals(listeningAddress),
          "Listening addresses do not match.");
      identityManager.setListeningAddress(me, protocol, address);
    }

    ProtocolModel model = Iterables.getFirst(models, null);
    ServerBootstrap bootstrap;
    try {
      model.enterScope();
      bootstrap = bootstrapProvider.get();
    } finally {
      model.exitScope();
    }
    bootstrap.setOptions(options);
    bootstrap.setPipelineFactory(factory);

    Channel channel = bootstrap.bind(address);
    channels.addChannel(channel, ProtocolConfigUtils.getProtocolSetFromModels(models));

    log.info("Address " + address + " bound for protocols " + models);
    return channel;
  }
}
