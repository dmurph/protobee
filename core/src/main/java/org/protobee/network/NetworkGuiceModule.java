package org.protobee.network;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.handler.codec.http.HttpMessageDecoder;
import org.jboss.netty.handler.codec.http.HttpMessageEncoder;
import org.protobee.guice.netty.NioServerSocketChannelFactoryProvider;
import org.protobee.guice.scopes.SessionScope;
import org.protobee.network.handlers.CleanupOnDisconnectHandler;
import org.protobee.network.handlers.CloseOnExceptionHandler;
import org.protobee.network.handlers.LoggingUpstreamHandler;
import org.protobee.network.handlers.MultipleRequestReceiver;
import org.protobee.network.handlers.SingleRequestReceiver;
import org.protobee.protocol.ProtocolConfig;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Named;


public class NetworkGuiceModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new FactoryModuleBuilder().build(MultipleRequestReceiver.Factory.class));

    bind(SingleRequestReceiver.class);
    bind(ChannelFactory.class).toProvider(NioServerSocketChannelFactoryProvider.class).in(
        Singleton.class);

    bind(ConnectionCreator.class).to(ConnectionCreatorImpl.class).in(Singleton.class);
    bind(ConnectionBinder.class).to(ConnectionBinderImpl.class).in(Singleton.class);

    bind(CleanupOnDisconnectHandler.class).in(SessionScope.class);
    bind(CloseOnExceptionHandler.class).in(Singleton.class);
    bind(LoggingUpstreamHandler.class).in(Singleton.class);
  }

  @Provides
  public ServerBootstrap getServerBootstrap(ChannelFactory factory) {
    return new ServerBootstrap(factory);
  }
}
