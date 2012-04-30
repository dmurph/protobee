package org.protobee.identity;

import java.net.SocketAddress;
import java.util.Map;
import java.util.Set;

import org.protobee.guice.IdentityScope;
import org.protobee.guice.IdentityScopeMap;
import org.protobee.guice.ProtobeeScopes;
import org.protobee.guice.ScopeHolder;
import org.protobee.protocol.Protocol;
import org.protobee.session.SessionManager;
import org.protobee.session.SessionModel;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Key;

/**
 * An identity on the network. This class is a scope holder for it's identity scope, and also
 * contains {@link ProtocolData} for each protocol registered in protobee.
 * 
 * @author Daniel
 */
@IdentityScope
public class NetworkIdentity implements ScopeHolder {

  private final Map<Protocol, ProtocolData> protocolData;
  private final Map<String, Object> identityScopeMap;
  private final Set<Object> tags = Sets.newHashSet();
  private final SessionManager sessionManager;
  private String description;

  @Inject
  public NetworkIdentity(Set<Protocol> protocols,
      @IdentityScopeMap Map<String, Object> identityScopeMap, SessionManager sessions) {
    this.sessionManager = sessions;
    this.identityScopeMap = identityScopeMap;
    ImmutableMap.Builder<Protocol, ProtocolData> builder = ImmutableMap.builder();

    for (Protocol protocol : protocols) {
      builder.put(protocol, new ProtocolData());
    }
    protocolData = builder.build();
    // this should happen automatically because this object is in the identity scope, but we'll keep
    // this here in case of subclasses
    ProtobeeScopes.putObjectInScope(Key.get(NetworkIdentity.class), this, identityScopeMap);
  }

  public ProtocolData getProtocolData(Protocol protocol) {
    ProtocolData data = protocolData.get(protocol);
    Preconditions.checkArgument(data != null, "Protocol was not in injected set.");
    return data;
  }

  public void clearCurrentSession(Protocol protocol) {
    ProtocolData data = getProtocolData(protocol);
    synchronized (data) {
      if (data.currentSession == null) {
        return;
      }
      sessionManager.removeCurrentSession(protocol, data.currentSession);
      data.currentSession = null;
    }
  }

  public boolean hasCurrentSession(Protocol protocol) {
    ProtocolData data = getProtocolData(protocol);
    return data.currentSession != null;
  }

  public SessionModel getCurrentSession(Protocol protocol) {
    ProtocolData data = getProtocolData(protocol);
    return data.currentSession;
  }

  public void registerNewSession(Protocol protocol, SessionModel session) {
    Preconditions.checkNotNull(session);
    ProtocolData data = getProtocolData(protocol);
    synchronized (data) {
      Preconditions.checkState(data.currentSession == null, "There's already a current session");
      protocolData.get(protocol).currentSession = session;
      sessionManager.registerNewSession(protocol, session);
    }
  }

  public SocketAddress getSendingAddress(Protocol protocol) {
    ProtocolData data = getProtocolData(protocol);
    return data.sendingAddress;
  }

  public SocketAddress getListeningAddress(Protocol protocol) {
    ProtocolData data = getProtocolData(protocol);
    return data.listeningAddress;
  }

  void setSendingAddress(Protocol protocol, SocketAddress address) {
    ProtocolData data = getProtocolData(protocol);
    data.sendingAddress = address;
  }

  void setListeningAddress(Protocol protocol, SocketAddress address) {
    ProtocolData data = getProtocolData(protocol);
    data.listeningAddress = address;
  }

  /**
   * returns an immutable copy of this object's tags
   * 
   * @return
   */
  public Set<Object> getTags() {
    return ImmutableSet.copyOf(tags);
  }

  void addTag(Object tag) {
    tags.add(tag);
  }

  public boolean hasTag(Object tag) {
    return tags.contains(tag);
  }

  Map<String, Object> getIdentityScopeMap() {
    return identityScopeMap;
  }

  @Override
  public final int hashCode() {
    return super.hashCode();
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public String toString() {
    return "{ description: " + description + ", tags: " + tags.toString() + ", protocolData: "
        + protocolData + "}";
  }

  public void enterScope() {
    Preconditions.checkState(!ProtobeeScopes.isInIdentityScope(), "Already in a session scope");
    ProtobeeScopes.enterIdentityScope(identityScopeMap);
  }

  public boolean isInScope() {
    return ProtobeeScopes.isInIdentityScope(identityScopeMap);
  }

  public void addObjectToScope(Key<?> key, Object object) {
    ProtobeeScopes.putObjectInScope(key, object, identityScopeMap);
  }

  public void exitScope() {
    ProtobeeScopes.exitIdentityScope();
  }

  public static class ProtocolData {
    private SocketAddress sendingAddress = null;
    private SocketAddress listeningAddress = null;
    private SessionModel currentSession = null;

    public SessionModel getCurrentSession() {
      return currentSession;
    }

    public SocketAddress getListeningAddress() {
      return listeningAddress;
    }

    public SocketAddress getSendingAddress() {
      return sendingAddress;
    }
  }
}
