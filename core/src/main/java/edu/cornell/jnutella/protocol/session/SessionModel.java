package edu.cornell.jnutella.protocol.session;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.MapMaker;
import com.google.inject.Key;

import edu.cornell.jnutella.guice.JnutellaScopes;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.modules.ProtocolModule;

/**
 * On construction, puts itself and the network identity into it's scope map
 * 
 * @author Daniel
 */
public abstract class SessionModel {

  private final NetworkIdentity identity;
  private SessionState sessionState;
  private final Set<ProtocolModule> modules;

  private final Map<String, Object> sessionScopeMap = new MapMaker().concurrencyLevel(4).makeMap();

  public SessionModel(NetworkIdentity identity, Set<ProtocolModule> mutableModules) {
    this.identity = identity;
    this.modules = mutableModules;
    JnutellaScopes.putObjectInScope(Key.get(SessionModel.class), this, sessionScopeMap);
    JnutellaScopes.putObjectInScope(Key.get(NetworkIdentity.class), identity, sessionScopeMap);
  }

  public NetworkIdentity getIdentity() {
    return identity;
  }

  public SessionState getSessionState() {
    return sessionState;
  }

  public void setSessionState(SessionState sessionState) {
    this.sessionState = sessionState;
  }

  public Set<ProtocolModule> getModules() {
    return modules;
  }

  Map<String, Object> getSessionScopeMap() {
    return sessionScopeMap;
  }

  public void enterScope() {
    JnutellaScopes.enterSessionScope(sessionScopeMap);
  }

  public boolean isInScope() {
    return JnutellaScopes.isInSessionScope(sessionScopeMap);
  }

  public void addObjectToScope(Key<?> key, Object object) {
    JnutellaScopes.putObjectInScope(key, object, sessionScopeMap);
  }

  public void exitScope() {
    JnutellaScopes.exitSessionScope();
  }
}
