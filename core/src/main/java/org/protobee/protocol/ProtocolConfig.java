package org.protobee.protocol;

import java.net.SocketAddress;
import java.util.Map;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.http.HttpMessageDecoder;
import org.jboss.netty.handler.codec.http.HttpMessageEncoder;
import org.protobee.session.ProtocolModulesHolder;

import com.google.inject.Provider;


public interface ProtocolConfig extends Provider<Protocol> {

  /**
   * Precondition: we are in the session scope, but the SessionModel is not yet in the scope.
   * 
   * @return
   */
  ProtocolModulesHolder createSessionModel();

  /**
   * Precondition: we are correct session and identity scope
   * 
   * @return
   */
  ChannelHandler[] createProtocolHandlers();

  HttpMessageDecoder createRequestDecoder();

  HttpMessageEncoder createRequestEncoder();

  /**
   * Options set on the server. Should include "child" options that match the
   * {@link #getConnectionOptions()}
   * 
   * @return
   */
  Map<String, Object> getServerBootstrapOptions();

  /**
   * Options set on connection channels. should match the 'child' options in
   * {@link #getServerBootstrapOptions()}
   * 
   * @return
   */
  Map<String, Object> getConnectionOptions();

  /**
   * The local address used for listening
   * 
   * @return
   */
  SocketAddress getListeningAddress();
}
