package edu.cornell.jnutella.protocol;

import java.util.Map;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.http.HttpMessageDecoder;
import org.jboss.netty.handler.codec.http.HttpMessageEncoder;

import com.google.inject.Provider;

import edu.cornell.jnutella.identity.ProtocolIdentityModel;
import edu.cornell.jnutella.session.ProtocolSessionModel;

public interface ProtocolConfig extends Provider<Protocol> {

  /**
   * Precondition: we are in the session scope, but the SessionModel is not yet in the scope.
   * 
   * @return
   */
  ProtocolSessionModel createSessionModel();

  ChannelHandler[] createProtocolHandlers();

  ProtocolIdentityModel createIdentityModel();

  HttpMessageDecoder createRequestDecoder();

  HttpMessageEncoder createRequestEncoder();

  Map<String, Object> getNettyBootstrapOptions();
}
