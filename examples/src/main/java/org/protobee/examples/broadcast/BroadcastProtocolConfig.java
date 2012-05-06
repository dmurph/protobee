package org.protobee.examples.broadcast;

import java.net.SocketAddress;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.channel.ChannelHandler;
import org.protobee.examples.broadcast.constants.BroadcastListeningAddress;
import org.protobee.modules.ProtocolModule;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.ProtocolConfig;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;

@Protocol(name = "BROADCAST", majorVersion = 0, minorVersion = 1, headerRegex = "^SAY / BROADCAST/0\\.1$")
public class BroadcastProtocolConfig extends ProtocolConfig {

  private final Provider<ChannelHandler[]> protocolHandlers;
  private final Provider<Set<ProtocolModule>> modules;
  private final Provider<Set<Class<? extends ProtocolModule>>> moduleClasses;
  private final SocketAddress listeningAddress;

  @Inject
  public BroadcastProtocolConfig(@Broadcast Provider<ChannelHandler[]> protocolHandlers,
      @Broadcast Provider<Set<ProtocolModule>> modules,
      @Broadcast Provider<Set<Class<? extends ProtocolModule>>> moduleClasses,
      @BroadcastListeningAddress SocketAddress listeningAddress) {
    this.protocolHandlers = protocolHandlers;
    this.modules = modules;
    this.moduleClasses = moduleClasses;
    this.listeningAddress = listeningAddress;
  }

  @Override
  public Set<ProtocolModule> createProtocolModules() {
    return modules.get();
  }

  @Override
  public Set<Class<? extends ProtocolModule>> getModuleClasses() {
    return moduleClasses.get();
  }

  @Override
  public ChannelHandler[] createProtocolHandlers() {
    return protocolHandlers.get();
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
  public SocketAddress getListeningAddress() {
    return listeningAddress;
  }
}
