package org.protobee.examples.questions;

import java.net.SocketAddress;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.channel.ChannelHandler;
import org.protobee.modules.ProtocolModule;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.ProtocolConfig;

@Protocol(name = "QUESTIONS", majorVersion = 0, minorVersion = 1, headerRegex = "^ASK / QUESTIONS/0\\.1$")
public class QuestionsProtocolConfig extends ProtocolConfig {

  @Override
  public Set<ProtocolModule> createProtocolModules() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Set<Class<? extends ProtocolModule>> getModuleClasses() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ChannelHandler[] createProtocolHandlers() {
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
