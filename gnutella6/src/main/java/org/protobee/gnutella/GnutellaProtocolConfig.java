package org.protobee.gnutella;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
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

  private final Provider<GnutellaHttpRequestDecoder> decoderProvider;
  private final Provider<GnutellaHttpRequestEncoder> encoderProvider;

  @Inject
  public GnutellaProtocolConfig(Provider<NioServerSocketChannelFactory> serverFactory,
      Provider<NioClientSocketChannelFactory> clientFactory,
      @Gnutella Provider<ChannelHandler[]> channelsProvider,
      @Gnutella Provider<Set<ProtocolModule>> gnutellaModules,
      @Gnutella Set<Class<? extends ProtocolModule>> gnutellaModuleClasses,
      Provider<GnutellaHttpRequestDecoder> decoderProvider,
      Provider<GnutellaHttpRequestEncoder> encoderProvider) {
    super(clientFactory, serverFactory, gnutellaModules, gnutellaModuleClasses, channelsProvider,
        new InetSocketAddress(6346));
    this.decoderProvider = decoderProvider;
    this.encoderProvider = encoderProvider;
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
  public HttpMessageDecoder createRequestDecoder() {
    return decoderProvider.get();
  }

  @Override
  public HttpMessageEncoder createRequestEncoder() {
    return encoderProvider.get();
  }
}
