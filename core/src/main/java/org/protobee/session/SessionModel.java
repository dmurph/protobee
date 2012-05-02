package org.protobee.session;

import org.protobee.guice.scopes.ProtocolScopeHolder;
import org.protobee.guice.scopes.ScopeHolder;
import org.protobee.guice.scopes.SessionScope;
import org.protobee.guice.scopes.SessionScopeHolder;
import org.protobee.identity.NetworkIdentity;
import org.protobee.protocol.ProtocolModel;

import com.google.inject.Inject;
import com.google.inject.Key;

@SessionScope
public class SessionModel {

  private final NetworkIdentity identity;
  private final ScopeHolder scope;
  private final ProtocolModel protocol;

  private SessionState sessionState;

  @Inject
  public SessionModel(NetworkIdentity identity, @SessionScopeHolder ScopeHolder scope,
      ProtocolModel protocol) {
    this.identity = identity;
    this.scope = scope;
    this.protocol = protocol;

    // in case we're subclassed, make sure we include ourself as a session model in the scope
    scope.putInScope(Key.get(SessionModel.class), this);
  }

  public NetworkIdentity getIdentity() {
    return identity;
  }

  public ProtocolModel getProtocol() {
    return protocol;
  }

  public SessionState getSessionState() {
    return sessionState;
  }

  public void setSessionState(SessionState sessionState) {
    this.sessionState = sessionState;
  }

  public ScopeHolder getScope() {
    return scope;
  }

  public void enterScope() {
    scope.enterScope();
  }

  public boolean isInScope() {
    return scope.isInScope();
  }

  public void exitScope() {
    scope.exitScope();
  }
}
