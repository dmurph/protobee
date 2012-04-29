package org.protobee.network;

import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.handler.codec.http.HttpMessageDecoder;
import org.jboss.netty.handler.codec.http.HttpMessageEncoder;
import org.protobee.JnutellaServantBootstrapper;
import org.protobee.guice.SessionScope;
import org.protobee.guice.netty.NioServerSocketChannelFactoryProvider;
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
    install(new FactoryModuleBuilder().build(SingleRequestReceiver.Factory.class));

    bind(ChannelFactory.class).toProvider(NioServerSocketChannelFactoryProvider.class).in(Singleton.class);

    bind(HandshakeHttpMessageDecoder.class).in(SessionScope.class);
    bind(HandshakeHttpMessageEncoder.class).in(SessionScope.class);

    bind(ConnectionCreator.class).to(ConnectionCreatorImpl.class).in(Singleton.class);
    bind(ConnectionBinder.class).to(ConnectionBinderImpl.class).in(Singleton.class);
    bind(JnutellaServantBootstrapper.class).in(Singleton.class);
  }

  @Provides
  @Named("request")
  @SessionScope
  public HttpMessageDecoder getRequestDecoder(ProtocolConfig config) {
    return config.createRequestDecoder();
  }

  @Provides
  @Named("request")
  @SessionScope
  public HttpMessageEncoder getRequestEncoder(ProtocolConfig config) {
    return config.createRequestEncoder();
  }
}
