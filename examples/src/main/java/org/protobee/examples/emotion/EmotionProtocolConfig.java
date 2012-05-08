package org.protobee.examples.emotion;

import java.net.SocketAddress;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.local.LocalClientChannelFactory;
import org.jboss.netty.channel.local.LocalServerChannelFactory;
import org.protobee.examples.emotion.constants.EmotionListeningAddress;
import org.protobee.modules.ProtocolModule;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.ProtocolConfig;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;

@Protocol(name = "EMOTION", majorVersion = 0, minorVersion = 1, headerRegex = "^SAY / EMOTION/0\\.1$")
public class EmotionProtocolConfig extends ProtocolConfig {

  @Inject
  public EmotionProtocolConfig(Provider<LocalServerChannelFactory> serverProvider,
      Provider<LocalClientChannelFactory> clientProvider,
      @Emotion Provider<ChannelHandler[]> protocolHandlers,
      @Emotion Provider<Set<ProtocolModule>> modules,
      @Emotion Set<Class<? extends ProtocolModule>> moduleClasses,
      @EmotionListeningAddress SocketAddress listeningAddress) {
    super(clientProvider, serverProvider, modules, moduleClasses, protocolHandlers,
        listeningAddress);
  }

  @Override
  public Map<String, Object> getServerOptions() {
    return Maps.newHashMap();
  }

  @Override
  public Map<String, Object> getConnectionOptions() {
    return Maps.newHashMap();
  }
}
