package edu.cornell.jnutella.identity;

import java.net.SocketAddress;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Key;

import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.guice.IdentityScope;
import edu.cornell.jnutella.guice.IdentityScopeMap;
import edu.cornell.jnutella.guice.JnutellaScopes;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.session.SessionManager;
import edu.cornell.jnutella.session.SessionModel;

@IdentityScope
public class NetworkIdentity {

  @InjectLogger
  private Logger log;
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
    JnutellaScopes.putObjectInScope(Key.get(NetworkIdentity.class), this, identityScopeMap);
  }

  private ProtocolData getProtocolData(Protocol protocol) {
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

  public SocketAddress getAddress(Protocol protocol) {
    ProtocolData data = getProtocolData(protocol);
    return data.address;
  }

  void setNewtorkAddress(Protocol protocol, SocketAddress address) {
    ProtocolData data = getProtocolData(protocol);
    data.address = address;
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
    return "{ description: " + description + ", tags: " + tags.toString() + ", protocolModels: "
        + protocolData + "}";
  }

  public void enterScope() {
    JnutellaScopes.enterIdentityScope(identityScopeMap);
  }

  public boolean isInScope() {
    return JnutellaScopes.isInIdentityScope(identityScopeMap);
  }

  public void addObjectToScope(Key<?> key, Object object) {
    JnutellaScopes.putObjectInScope(key, object, identityScopeMap);
  }

  public void exitScope() {
    JnutellaScopes.exitIdentityScope();
  }

  static class ProtocolData {
    private SocketAddress address = null;
    private SessionModel currentSession = null;
  }
}
