package org.protobee.guice.netty;

import org.jboss.netty.channel.local.DefaultLocalClientChannelFactory;
import org.jboss.netty.channel.local.DefaultLocalServerChannelFactory;
import org.jboss.netty.channel.local.LocalClientChannelFactory;
import org.jboss.netty.channel.local.LocalServerChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.channel.socket.oio.OioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.oio.OioServerSocketChannelFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class NettyGuiceModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(NioClientSocketChannelFactory.class).toProvider(
        NioClientSocketChannelFactoryProvider.class).in(Singleton.class);
    bind(NioServerSocketChannelFactory.class).toProvider(
        NioServerSocketChannelFactoryProvider.class).in(Singleton.class);
    bind(OioClientSocketChannelFactory.class).toProvider(
        OioClientSocketChannelFactoryProvider.class).in(Singleton.class);
    bind(OioServerSocketChannelFactory.class).toProvider(
        OioServerSocketChannelFactoryProvider.class).in(Singleton.class);
  }

  @Provides
  @Singleton
  public LocalClientChannelFactory getLocalClientChannelFactory() {
    return new DefaultLocalClientChannelFactory();
  }

  @Provides
  @Singleton
  public LocalServerChannelFactory getLocalServerChannelFactory() {
    return new DefaultLocalServerChannelFactory();
  }
}
