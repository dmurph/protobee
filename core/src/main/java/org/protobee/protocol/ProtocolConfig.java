package org.protobee.protocol;

import java.util.Map;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.http.HttpMessageDecoder;
import org.jboss.netty.handler.codec.http.HttpMessageEncoder;
import org.protobee.session.ProtocolSessionModel;

import com.google.inject.Provider;


public interface ProtocolConfig extends Provider<Protocol> {

  /**
   * Precondition: we are in the session scope, but the SessionModel is not yet in the scope.
   * 
   * @return
   */
  ProtocolSessionModel createSessionModel();

  ChannelHandler[] createProtocolHandlers();

  HttpMessageDecoder createRequestDecoder();

  HttpMessageEncoder createRequestEncoder();

  Map<String, Object> getNettyBootstrapOptions();
  
  int getPort();
}
