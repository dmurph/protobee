package edu.cornell.jnutella.guice.netty;

import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
import org.jboss.netty.channel.socket.ServerSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.channel.socket.oio.OioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.oio.OioServerSocketChannelFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import edu.cornell.jnutella.network.NetworkGuiceModule;

/**
 * Module to make the channel factories in Netty bindable. Not specific to the jnutella application
 * at all, see {@link NetworkGuiceModule}.
 * 
 * @author Daniel
 */
public class NettyModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(ClientSocketChannelFactory.class).toProvider(NioClientSocketChannelFactoryProvider.class)
        .in(Scopes.SINGLETON);

    bind(ServerSocketChannelFactory.class).toProvider(NioServerSocketChannelFactoryProvider.class)
        .in(Scopes.SINGLETON);

    bind(NioClientSocketChannelFactory.class).toProvider(
        NioClientSocketChannelFactoryProvider.class).in(Scopes.SINGLETON);

    bind(NioServerSocketChannelFactory.class).toProvider(
        NioServerSocketChannelFactoryProvider.class).in(Scopes.SINGLETON);

    bind(OioClientSocketChannelFactory.class).toProvider(
        OioClientSocketChannelFactoryProvider.class).in(Scopes.SINGLETON);

    bind(OioServerSocketChannelFactory.class).toProvider(
        OioServerSocketChannelFactoryProvider.class).in(Scopes.SINGLETON);
  }
}
