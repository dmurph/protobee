package org.protobee.util;

import org.protobee.guice.scopes.IdentityScopeHolder;
import org.protobee.guice.scopes.ProtobeeScopes;
import org.protobee.guice.scopes.ProtocolScopeHolder;
import org.protobee.guice.scopes.ScopeHolder;
import org.protobee.guice.scopes.SessionScopeHolder;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;


/**
 * Utility class for descoping + rescoping any current identity, session, or protocol scopes
 * 
 * @author Daniel
 */
public class Descoper {

  private final Provider<ScopeHolder> identityScopeProvider;
  private final Provider<ScopeHolder> sessionScopeProvider;
  private final Provider<ScopeHolder> protocolScopeProvider;

  private ScopeHolder identityScope = null;
  private ScopeHolder sessionScope = null;
  private ScopeHolder protocolScope = null;

  @Inject
  public Descoper(@IdentityScopeHolder Provider<ScopeHolder> identityScopeProvider,
      @SessionScopeHolder Provider<ScopeHolder> sessionScopeProvider,
      @ProtocolScopeHolder Provider<ScopeHolder> protocolScopeProvider) {
    this.identityScopeProvider = identityScopeProvider;
    this.sessionScopeProvider = sessionScopeProvider;
    this.protocolScopeProvider = protocolScopeProvider;
  }

  /**
   * This should be in a try-finally block, where the subsequent matching {@link #rescope()} is in
   * the finally block
   */
  public void descope() {
    Preconditions.checkState(identityScope == null, "Must rescope before descoping again");
    Preconditions.checkState(sessionScope == null, "Must rescope before descoping again");
    Preconditions.checkState(protocolScope == null, "Must rescope before descoping again");

    if (ProtobeeScopes.IDENTITY.isInScope()) {
      identityScope = identityScopeProvider.get();
      identityScope.exitScope();
    }
    if (ProtobeeScopes.SESSION.isInScope()) {
      sessionScope = sessionScopeProvider.get();
      sessionScope.exitScope();
    }
    if (ProtobeeScopes.PROTOCOL.isInScope()) {
      protocolScope = protocolScopeProvider.get();
      protocolScope.exitScope();
    }
  }

  public void rescope() {
    if (identityScope != null) {
      identityScope.enterScope();
      identityScope = null;
    }

    if (sessionScope != null) {
      sessionScope.enterScope();
      sessionScope = null;
    }

    if (protocolScope != null) {
      protocolScope.enterScope();
      protocolScope = null;
    }
  }
}
