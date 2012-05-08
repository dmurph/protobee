package org.protobee.protocol;

import java.net.SocketAddress;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.channel.ChannelFactory;
import org.protobee.guice.scopes.ProtocolScope;
import org.protobee.guice.scopes.ProtocolScopeHolder;
import org.protobee.guice.scopes.ScopeHolder;
import org.protobee.modules.ProtocolModule;
import org.protobee.util.ProtocolUtils;

import com.google.inject.Inject;

/**
 * This stores protocol data and the protocol scope, mostly populated from the config. Ideally, we
 * shouldn't be storing anything but the scope, but most of this stuff just muddies here because
 * it's in the scope already, but it cleans up a lot of classes and it's used as a 'key' anyways.
 * 
 * @author Daniel
 */
@ProtocolScope
public class ProtocolModel {

  private final ScopeHolder scope;
  private final Protocol protocol;
  private final Map<String, Object> serverOptions;
  private final Map<String, Object> connectionOptions;
  private final SocketAddress localListeningAddress;
  private final Set<Class<? extends ProtocolModule>> moduleClasses;
  private final ChannelFactory clientFactory;
  private final ChannelFactory serverFactory;

  @Inject
  public ProtocolModel(@ProtocolScopeHolder ScopeHolder scopeHolder, ProtocolConfig config) {
    this.scope = scopeHolder;
    this.protocol = config.get();
    this.serverOptions = config.getMergedServerOptions();
    this.connectionOptions = config.getConnectionOptions();
    this.localListeningAddress = config.getListeningAddress();
    this.moduleClasses = config.getModuleClasses();
    this.clientFactory = config.getClientChannelFactory();
    this.serverFactory = config.getServerChannelFactory();
  }

  public Set<Class<? extends ProtocolModule>> getModuleClasses() {
    return moduleClasses;
  }

  public Map<String, Object> getConnectionOptions() {
    return connectionOptions;
  }

  public Map<String, Object> getServerOptions() {
    return serverOptions;
  }

  public SocketAddress getLocalListeningAddress() {
    return localListeningAddress;
  }

  public Protocol getProtocol() {
    return protocol;
  }

  public ChannelFactory getClientFactory() {
    return clientFactory;
  }

  public ChannelFactory getServerFactory() {
    return serverFactory;
  }

  public ScopeHolder getScope() {
    return scope;
  }

  public void enterScope() {
    scope.enterScope();
  }

  public boolean isInScope() {
    return scope.isInScope();
  }

  public void exitScope() {
    scope.exitScope();
  }

  @Override
  public String toString() {
    return "{ protocolScope: " + scope + ", protocol: " + ProtocolUtils.toString(protocol) + "}";
  }
}
