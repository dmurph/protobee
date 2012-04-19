package edu.cornell.jnutella.gnutella;

import java.util.Map;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.http.HttpMessageDecoder;
import org.jboss.netty.handler.codec.http.HttpMessageEncoder;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;

import edu.cornell.jnutella.gnutella.session.GnutellaSessionModel;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.identity.ProtocolIdentityModel;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.protocol.ProtocolConfig;
import edu.cornell.jnutella.protocol.session.SessionModel;

@Protocol(name = "GNUTELLA", majorVersion = 0, minorVersion = 6, headerRegex = "^GNUTELLA CONNECT/0\\.6$")
public class GnutellaProtocolConfig implements ProtocolConfig {

  private final Provider<ChannelHandler[]> channelsProvider;
  private final Provider<GnutellaSessionModel> sessionModuleProvider;
  private final Provider<GnutellaIdentityModel> identityModelProvider;
  private final Provider<GnutellaHttpRequestDecoder> decoderProvider;
  private final Provider<GnutellaHttpRequestEncoder> encoderProvider;
  private final Protocol protocol;

  @Inject
  public GnutellaProtocolConfig(@Gnutella Provider<ChannelHandler[]> channelsProvider,
      Provider<GnutellaSessionModel> sessionModuleProvider,
      Provider<GnutellaIdentityModel> identityModelProvider,
      Provider<GnutellaHttpRequestDecoder> decoderProvider,
      Provider<GnutellaHttpRequestEncoder> encoderProvider) {
    this.channelsProvider = channelsProvider;
    this.sessionModuleProvider = sessionModuleProvider;
    this.identityModelProvider = identityModelProvider;
    this.protocol = this.getClass().getAnnotation(Protocol.class);
    this.decoderProvider = decoderProvider;
    this.encoderProvider = encoderProvider;
  }

  @Override
  public SessionModel createSessionModel() {
    return sessionModuleProvider.get();
  }

  @Override
  public ChannelHandler[] createProtocolHandlers() {
    return channelsProvider.get();
  }

  @Override
  public ProtocolIdentityModel createIdentityModel() {
    return identityModelProvider.get();
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
  public int getPort() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public Map<String, Object> getNettyBootstrapOptions() {
    return Maps.newHashMap();
  }
}
