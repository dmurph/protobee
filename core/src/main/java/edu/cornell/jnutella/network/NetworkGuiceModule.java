package edu.cornell.jnutella.network;

import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpMessageDecoder;
import org.jboss.netty.handler.codec.http.HttpMessageEncoder;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import edu.cornell.jnutella.guice.SessionScope;
import edu.cornell.jnutella.guice.netty.NettyModule;
import edu.cornell.jnutella.protocol.ProtocolConfig;

public class NetworkGuiceModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new NettyModule());

    bind(ReceivingRequestMultiplexer.class).in(Singleton.class);
    bind(ChannelFactory.class).to(NioServerSocketChannelFactory.class).in(Singleton.class);

    bind(HandshakeHttpMessageDecoder.class).in(SessionScope.class);
    bind(HandshakeHttpMessageEncoder.class).in(SessionScope.class);
    
    bind(NetworkManager.class).in(Singleton.class);
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