package org.protobee.identity;

import org.protobee.guice.scopes.NewIdentityScopeHolder;
import org.protobee.guice.scopes.ScopeHolder;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * Wraps the creation of a {@link NetworkIdentity} identity inside of a new identity scope
 * 
 * @author Daniel
 */
@Singleton
public class NetworkIdentityFactory {

  private Provider<NetworkIdentity> identityProvider;
  private final Provider<ScopeHolder> scopeProvider;

  @Inject
  public NetworkIdentityFactory(@NewIdentityScopeHolder Provider<ScopeHolder> scopeProvider,
      Provider<NetworkIdentity> identityProvider) {
    this.identityProvider = identityProvider;
    this.scopeProvider = scopeProvider;
  }

  /**
   * Preconditions: not in an identity scope
   * 
   * @return
   */
  public NetworkIdentity create() {
    ScopeHolder holder = scopeProvider.get();
    holder.enterScope();
    NetworkIdentity identity = identityProvider.get();
    holder.exitScope();
    return identity;
  }
}
