package org.protobee.gnutella;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.http.HttpMessageDecoder;
import org.jboss.netty.handler.codec.http.HttpMessageEncoder;
import org.protobee.gnutella.session.GnutellaSessionModel;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.ProtocolConfig;
import org.protobee.session.ProtocolModulesHolder;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;


@Singleton
@Protocol(name = "GNUTELLA", majorVersion = 0, minorVersion = 6, headerRegex = "^GNUTELLA CONNECT/0\\.6$")
public class GnutellaProtocolConfig implements ProtocolConfig {

  private final Provider<ChannelHandler[]> channelsProvider;
  private final Provider<GnutellaSessionModel> sessionModuleProvider;
  private final Provider<GnutellaHttpRequestDecoder> decoderProvider;
  private final Provider<GnutellaHttpRequestEncoder> encoderProvider;
  private final Protocol protocol;

  @Inject
  public GnutellaProtocolConfig(@Gnutella Provider<ChannelHandler[]> channelsProvider,
      Provider<GnutellaSessionModel> sessionModuleProvider,
      Provider<GnutellaHttpRequestDecoder> decoderProvider,
      Provider<GnutellaHttpRequestEncoder> encoderProvider) {
    this.channelsProvider = channelsProvider;
    this.sessionModuleProvider = sessionModuleProvider;
    this.protocol = this.getClass().getAnnotation(Protocol.class);
    this.decoderProvider = decoderProvider;
    this.encoderProvider = encoderProvider;
  }

  @Override
  public ProtocolModulesHolder createSessionModel() {
    return sessionModuleProvider.get();
  }

  @Override
  public ChannelHandler[] createProtocolHandlers() {
    return channelsProvider.get();
  }

  @Override
  public Protocol get() {
    return protocol;
  }

  @Override
  public HttpMessageDecoder createRequestDecoder() {
    return decoderProvider.get();
  }

  @Override
  public HttpMessageEncoder createRequestEncoder() {
    return encoderProvider.get();
  }

  @Override
  public Map<String, Object> getServerBootstrapOptions() {
    return Maps.newHashMap();
  }
  
  @Override
  public Map<String, Object> getConnectionOptions() {
    return Maps.newHashMap();
  }

  @Override
  public SocketAddress getListeningAddress() {
    return new InetSocketAddress(6346);
  }
}
