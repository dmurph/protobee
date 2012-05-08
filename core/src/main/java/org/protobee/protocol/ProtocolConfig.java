package org.protobee.protocol;

import java.net.SocketAddress;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.http.HttpMessageDecoder;
import org.jboss.netty.handler.codec.http.HttpMessageEncoder;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.protobee.modules.ProtocolModule;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.Provider;

/**
 * Describes a protocol.
 * 
 * @author Daniel
 */
public abstract class ProtocolConfig implements Provider<Protocol> {

  private final Protocol protocol;
  private final Provider<? extends ChannelFactory> clientChannelFactory;
  private final Provider<? extends ChannelFactory> serverChannelFactory;
  private final Provider<Set<ProtocolModule>> modulesProvider;
  private final Set<Class<? extends ProtocolModule>> moduleClasses;
  private final Provider<ChannelHandler[]> handlersProvider;
  private final SocketAddress localAddress;

  protected ProtocolConfig(Provider<? extends ChannelFactory> clientChannelFactory,
      Provider<? extends ChannelFactory> serverChannelFactory,
      Provider<Set<ProtocolModule>> modulesProvider,
      Set<Class<? extends ProtocolModule>> moduleClasses,
      Provider<ChannelHandler[]> handlersProvider, SocketAddress localListeningAddress) {
    this.clientChannelFactory = clientChannelFactory;
    this.serverChannelFactory = serverChannelFactory;
    this.modulesProvider = modulesProvider;
    this.moduleClasses = moduleClasses;
    this.handlersProvider = handlersProvider;
    this.localAddress = localListeningAddress;
    this.protocol = this.getClass().getAnnotation(Protocol.class);
    Preconditions.checkNotNull(protocol, "Protocol config must be annotated with @Protocol");
  }

  @Override
  public Protocol get() {
    return protocol;
  }

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

  public Set<ProtocolModule> createProtocolModules() {
    return modulesProvider.get();
  }

  public Set<Class<? extends ProtocolModule>> getModuleClasses() {
    return moduleClasses;
  }

  /**
   * Precondition: we are correct session and identity scope
   * 
   * @return
   */
  public ChannelHandler[] createProtocolHandlers() {
    return handlersProvider.get();
  }


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
  public SocketAddress getListeningAddress() {
    return localAddress;
  }

  public ChannelFactory getServerChannelFactory() {
    return serverChannelFactory.get();
  }

  public ChannelFactory getClientChannelFactory() {
    return clientChannelFactory.get();
  }
}
