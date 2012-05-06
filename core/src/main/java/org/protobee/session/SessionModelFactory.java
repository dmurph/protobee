package org.protobee.session;

import org.protobee.guice.scopes.IdentityScope;
import org.protobee.guice.scopes.NewSessionScopeHolder;
import org.protobee.guice.scopes.ScopeHolder;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Provider;


/**
 * Creates a session for a protocol in an identity, and wraps the creation of the session with a new
 * session scope
 * 
 * @author Daniel
 */
@IdentityScope
public class SessionModelFactory {

  private final Provider<SessionModel> sessionModelProvider;
  private final Provider<ScopeHolder> sessionScopeProvider;

  @Inject
  public SessionModelFactory(Provider<SessionModel> sessionModelProvider,
      @NewSessionScopeHolder Provider<ScopeHolder> sessionScopeProvider) {
    this.sessionModelProvider = sessionModelProvider;
    this.sessionScopeProvider = sessionScopeProvider;
  }

  /**
   * Precondition: was have to be in the respective entity and protocol scope
   * 
   * @param pconfig
   * @param eventBusLabel
   * @return
   */
  public SessionModel create(String eventBusLabel) {
    ScopeHolder scope = sessionScopeProvider.get();

    EventBus sessionEventBus = new EventBus(eventBusLabel);
    scope.putInScope(Key.get(EventBus.class), sessionEventBus);
    scope.enterScope();
    SessionModel sessionModel = sessionModelProvider.get();
    scope.exitScope();
    return sessionModel;
  }
}
