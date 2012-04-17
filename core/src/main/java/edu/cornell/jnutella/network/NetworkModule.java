package edu.cornell.jnutella.network;

import org.jboss.netty.bootstrap.Bootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import edu.cornell.jnutella.guice.netty.NettyModule;

public class NetworkModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new NettyModule());
    install(new FactoryModuleBuilder().build(HttpMessageDecoder.Factory.class));
    install(new FactoryModuleBuilder().build(HttpMessageEncoder.Factory.class));

    bind(Bootstrap.class).to(ServerBootstrap.class).in(Singleton.class);
    bind(ReceivingRequestMultiplexer.class).in(Singleton.class);
    bind(ChannelFactory.class).to(NioServerSocketChannelFactory.class).in(Singleton.class);
  }

}
