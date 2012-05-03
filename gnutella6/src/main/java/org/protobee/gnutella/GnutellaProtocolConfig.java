package org.protobee.gnutella;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.http.HttpMessageDecoder;
import org.jboss.netty.handler.codec.http.HttpMessageEncoder;
import org.protobee.modules.ProtocolModule;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.ProtocolConfig;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
@Protocol(name = "GNUTELLA", majorVersion = 0, minorVersion = 6, headerRegex = "^GNUTELLA CONNECT/0\\.6$")
public class GnutellaProtocolConfig extends ProtocolConfig {

  private final Provider<ChannelHandler[]> channelsProvider;
  private final Provider<GnutellaHttpRequestDecoder> decoderProvider;
  private final Provider<GnutellaHttpRequestEncoder> encoderProvider;
  private final Provider<Set<ProtocolModule>> gnutellaModules;
  private final Set<Class<? extends ProtocolModule>> gnutellaModuleClasses;
  
  @Inject
  public GnutellaProtocolConfig(@Gnutella Provider<ChannelHandler[]> channelsProvider,
      @Gnutella Provider<Set<ProtocolModule>> gnutellaModules,
      @Gnutella Set<Class<? extends ProtocolModule>> gnutellaModuleClasses,
      Provider<GnutellaHttpRequestDecoder> decoderProvider,
      Provider<GnutellaHttpRequestEncoder> encoderProvider) {
    this.channelsProvider = channelsProvider;
    this.decoderProvider = decoderProvider;
    this.encoderProvider = encoderProvider;
    this.gnutellaModules = gnutellaModules;
    this.gnutellaModuleClasses = gnutellaModuleClasses;
  }

  @Override
  public ChannelHandler[] createProtocolHandlers() {
    return channelsProvider.get();
  }

  @Override
  public Map<String, Object> getServerOptions() {
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

  @Override
  public Set<ProtocolModule> createProtocolModules() {
    return gnutellaModules.get();
  }

  @Override
  public Set<Class<? extends ProtocolModule>> getModuleClasses() {
    return gnutellaModuleClasses;
  }

  @Override
  public HttpMessageDecoder createRequestDecoder() {
    return decoderProvider.get();
  }

  @Override
  public HttpMessageEncoder createRequestEncoder() {
    return encoderProvider.get();
  }
}
