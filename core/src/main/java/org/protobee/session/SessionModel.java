package org.protobee.session;

import java.util.Map;

import org.protobee.guice.JnutellaScopes;
import org.protobee.guice.SessionScope;
import org.protobee.guice.SessionScopeMap;
import org.protobee.identity.NetworkIdentity;

import com.google.inject.Inject;
import com.google.inject.Key;


/**
 * On construction, puts itself and the network identity into it's scope map
 * 
 * @author Daniel
 */
@SessionScope
public class SessionModel {

  private final NetworkIdentity identity;
  private final Map<String, Object> sessionScopeMap;

  private SessionState sessionState;

  @Inject
  public SessionModel(NetworkIdentity identity, 
      @SessionScopeMap Map<String, Object> sessionScopeMap) {
    this.identity = identity;
    this.sessionScopeMap = sessionScopeMap;
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
