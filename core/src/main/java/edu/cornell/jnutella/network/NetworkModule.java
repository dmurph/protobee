package edu.cornell.jnutella.network;

import org.jboss.netty.bootstrap.Bootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import edu.cornell.jnutella.guice.netty.NettyModule;

public class NetworkModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new NettyModule());
    
    bind(Bootstrap.class).to(ServerBootstrap.class).in(Singleton.class);
    bind(ChannelFactory.class).to(NioServerSocketChannelFactory.class).in(Singleton.class);
  }

}
