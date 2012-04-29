package edu.cornell.jnutella.network;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.identity.NetworkIdentityManager;
import edu.cornell.jnutella.protocol.ProtocolConfig;
import edu.cornell.jnutella.util.ProtocolConfigUtils;

@Singleton
public class ConnectionBinderImpl implements ConnectionBinder {

  @InjectLogger
  private Logger log;
  private final NetworkIdentityManager identityManager;
  private final ChannelFactory channelFactory;
  private final MultipleRequestReceiver.Factory requestMultiplexerFactory;
  private final SingleRequestReceiver.Factory requestHandlerFactory;
  private final JnutellaChannels channels;

  @Inject
  public ConnectionBinderImpl(NetworkIdentityManager identityManager,
      ChannelFactory channelFactory, MultipleRequestReceiver.Factory multiplexer,
      SingleRequestReceiver.Factory requestHandlerFactory, JnutellaChannels channels) {
    this.identityManager = identityManager;
    this.channelFactory = channelFactory;
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

    ServerBootstrap bootstrap = new ServerBootstrap(channelFactory);
    bootstrap.setOptions(config.getNettyBootstrapOptions());
    bootstrap.setPipelineFactory(factory);

    InetSocketAddress localAddress = new InetSocketAddress(config.getPort());
    // yes, this means our address will be a localhost address for a little bit. This should change
    // when we get the 'Remote-IP' header for the first time
    identityManager.setListeningAddress(identityManager.getMe(), config.get(), localAddress);

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

    ServerBootstrap bootstrap = new ServerBootstrap(channelFactory);
    bootstrap.setOptions(options);
    bootstrap.setPipelineFactory(factory);

    InetSocketAddress localAddress = new InetSocketAddress(port);

    // yes, this means our address will be a localhost address for a little bit. This should change
    // when we get the 'Remote-IP' header for the first time
    NetworkIdentity me = identityManager.getMe();
    for (ProtocolConfig protocolConfig : configs) {
      identityManager.setListeningAddress(me, protocolConfig.get(), localAddress);
    }
    Channel channel = bootstrap.bind(new InetSocketAddress(port));
    channels.addChannel(channel, ProtocolConfigUtils.getProtocolSet(configs));

    log.info("Port " + port + " bound for protocols " + configs);
    return channel;
  }
}
