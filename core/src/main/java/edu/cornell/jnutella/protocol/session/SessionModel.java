package edu.cornell.jnutella.protocol.session;

import java.util.Set;

import org.jboss.netty.channel.Channel;

import com.google.common.eventbus.EventBus;

import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.identity.ProtocolIdentityModel;
import edu.cornell.jnutella.modules.ProtocolModule;
import edu.cornell.jnutella.protocol.Protocol;

public abstract class SessionModel {

  private final Channel channel;
  private final NetworkIdentity identity;
  private final Protocol protocol;
  private SessionState sessionState;
  private final EventBus eventBus;
  private final Set<ProtocolModule> modules;

  public SessionModel(Channel channel, Protocol protocol, NetworkIdentity identity,
      EventBus eventBus, Set<ProtocolModule> mutableModules) {
    this.channel = channel;
    this.protocol = protocol;
    this.identity = identity;
    this.eventBus = eventBus;
    this.modules = mutableModules;
  }

  public Channel getChannel() {
    return channel;
  }

  public Protocol getProtocol() {
    return protocol;
  }

  public NetworkIdentity getIdentity() {
    return identity;
  }

  public ProtocolIdentityModel getProtocolModel() {
    return identity.getModel(protocol);
  }

  public SessionState getSessionState() {
    return sessionState;
  }

  public void setSessionState(SessionState sessionState) {
    this.sessionState = sessionState;
  }

  public EventBus getEventBus() {
    return eventBus;
  }

  public Set<ProtocolModule> getModules() {
    return modules;
  }
}
