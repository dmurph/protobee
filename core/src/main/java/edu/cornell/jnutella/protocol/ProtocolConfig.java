package edu.cornell.jnutella.protocol;

import java.util.Map;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.http.HttpMessageDecoder;
import org.jboss.netty.handler.codec.http.HttpMessageEncoder;

import com.google.inject.Provider;

import edu.cornell.jnutella.identity.ProtocolIdentityModel;
import edu.cornell.jnutella.protocol.session.SessionModel;

public interface ProtocolConfig extends Provider<Protocol> {

  SessionModel createSessionModel();

  ChannelHandler[] createProtocolHandlers();

  ProtocolIdentityModel createIdentityModel();
  
  HttpMessageDecoder createRequestDecoder();
  
  HttpMessageEncoder createRequestEncoder();
  
  int getPort();
  
  Map<String, Object> getNettyBootstrapOptions();
}
