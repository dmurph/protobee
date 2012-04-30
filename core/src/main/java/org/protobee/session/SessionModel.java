package org.protobee.session;

import java.util.Map;

import org.protobee.guice.ProtobeeScopes;
import org.protobee.guice.ScopeHolder;
import org.protobee.guice.SessionScope;
import org.protobee.guice.SessionScopeMap;
import org.protobee.identity.NetworkIdentity;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Key;


/**
 * On construction, puts itself and the network identity into it's scope map
 * Preconditions for injection: we're in the respective identity scope
 * 
 * @author Daniel
 */
@SessionScope
public class SessionModel implements ScopeHolder {

  private final NetworkIdentity identity;
  private final Map<String, Object> sessionScopeMap;

  private SessionState sessionState;

  @Inject
  public SessionModel(NetworkIdentity identity, 
      @SessionScopeMap Map<String, Object> sessionScopeMap) {
    this.identity = identity;
    this.sessionScopeMap = sessionScopeMap;
    ProtobeeScopes.putObjectInScope(Key.get(SessionModel.class), this, sessionScopeMap);
    ProtobeeScopes.putObjectInScope(Key.get(NetworkIdentity.class), identity, sessionScopeMap);
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
    Preconditions.checkState(!ProtobeeScopes.isInSessionScope(), "Already in a session scope");
    ProtobeeScopes.enterSessionScope(sessionScopeMap);
  }

  public boolean isInScope() {
    return ProtobeeScopes.isInSessionScope(sessionScopeMap);
  }

  public void addObjectToScope(Key<?> key, Object object) {
    ProtobeeScopes.putObjectInScope(key, object, sessionScopeMap);
  }

  public void exitScope() {
    ProtobeeScopes.exitSessionScope();
  }
}
