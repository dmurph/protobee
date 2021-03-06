package org.protobee.examples.broadcast;

import java.net.SocketAddress;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ServerChannelFactory;
import org.protobee.examples.broadcast.constants.BroadcastListeningAddress;
import org.protobee.modules.ProtocolModule;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.ProtocolConfig;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;

@Protocol(name = "BROADCAST", majorVersion = 0, minorVersion = 1, headerRegex = "^SAY / BROADCAST/0\\.1$")
public class BroadcastProtocolConfig extends ProtocolConfig {

  @Inject
  public BroadcastProtocolConfig(@Broadcast Provider<ServerChannelFactory> serverProvider,
      @Broadcast Provider<ChannelFactory> clientProvider,
      @Broadcast Provider<ChannelHandler[]> protocolHandlers,
      @Broadcast Provider<Set<ProtocolModule>> modules,
      @Broadcast Set<Class<? extends ProtocolModule>> moduleClasses,
      @BroadcastListeningAddress SocketAddress listeningAddress) {
    super(clientProvider, serverProvider, modules, moduleClasses, protocolHandlers,
        listeningAddress);
  }

  @Override
  public Map<String, Object> getServerOptions() {
    Map<String, Object> map = Maps.newHashMap();
    map.put("reuseAddress", true);
    return map;
  }

  @Override
  public Map<String, Object> getConnectionOptions() {
    Map<String, Object> map = Maps.newHashMap();
    map.put("keepAlive", true);
    map.put("tcpNoDelay", true);
    return map;
  }
}
