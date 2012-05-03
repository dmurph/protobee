package org.protobee.network;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.protobee.guice.netty.NioServerSocketChannelFactoryProvider;
import org.protobee.guice.scopes.SessionScope;
import org.protobee.network.handlers.CleanupOnDisconnectHandler;
import org.protobee.network.handlers.CloseOnExceptionHandler;
import org.protobee.network.handlers.LoggingUpstreamHandler;
import org.protobee.network.handlers.MultipleRequestReceiver;
import org.protobee.network.handlers.SingleRequestReceiver;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;


public class NetworkGuiceModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new FactoryModuleBuilder().build(MultipleRequestReceiver.Factory.class));

    bind(SingleRequestReceiver.class);
    bind(ChannelFactory.class).toProvider(NioServerSocketChannelFactoryProvider.class).in(
        Singleton.class);

    bind(ProtobeeMessageWriter.class).to(ProtobeeMessageWriterImpl.class).in(Singleton.class);

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
