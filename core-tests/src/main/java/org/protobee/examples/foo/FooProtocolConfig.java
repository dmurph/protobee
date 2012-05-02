package org.protobee.examples.foo;

import java.net.SocketAddress;
import java.util.Map;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.http.HttpMessageDecoder;
import org.jboss.netty.handler.codec.http.HttpMessageEncoder;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.ProtocolConfig;
import org.protobee.session.SessionProtocolModules;

@Protocol(name = "FOO", majorVersion = 0, minorVersion = 1, headerRegex = "^TEST **FOO/0\\.1$")
public class FooProtocolConfig implements ProtocolConfig {

  
  
  
  @Override
  public Protocol get() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SessionProtocolModules createSessionModel() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ChannelHandler[] createProtocolHandlers() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public HttpMessageDecoder createRequestDecoder() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public HttpMessageEncoder createRequestEncoder() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<String, Object> getServerOptions() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<String, Object> getConnectionOptions() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SocketAddress getListeningAddress() {
    // TODO Auto-generated method stub
    return null;
  }

}
