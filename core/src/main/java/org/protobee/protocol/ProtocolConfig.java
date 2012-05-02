package org.protobee.protocol;

import java.net.SocketAddress;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.http.HttpMessageDecoder;
import org.jboss.netty.handler.codec.http.HttpMessageEncoder;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.protobee.modules.ProtocolModule;

import com.google.common.collect.Maps;
import com.google.inject.Provider;

public abstract class ProtocolConfig implements Provider<Protocol> {



  public HttpMessageDecoder createRequestDecoder() {
    return new HttpRequestDecoder();
  }

  public HttpMessageEncoder createRequestEncoder() {
    return new HttpRequestEncoder();
  }

  public Map<String, Object> getMergedServerOptions() {
    Map<String, Object> map = Maps.newHashMap();
    map.putAll(getServerOptions());
    for (Entry<String, Object> entry : getConnectionOptions().entrySet()) {
      map.put("child." + entry.getKey(), entry.getValue());
    }
    return map;
  }

  /**
   * Called in the corresponding protocol scope for this config when the scope is created.
   */
  public void scopedInit() {}

  public abstract Set<ProtocolModule> createProtocolModules();

  public abstract Set<Class<? extends ProtocolModule>> getModuleClasses();

  /**
   * Precondition: we are correct session and identity scope
   * 
   * @return
   */
  public abstract ChannelHandler[] createProtocolHandlers();


  /**
   * Options set on the server. "child" options are generated later from
   * {@link #getConnectionOptions()}
   * 
   * @return
   */
  public abstract Map<String, Object> getServerOptions();

  /**
   * Options set on connection channels.
   * 
   * @return
   */
  public abstract Map<String, Object> getConnectionOptions();

  /**
   * The local address used for listening
   * 
   * @return
   */
  public abstract SocketAddress getListeningAddress();
}
