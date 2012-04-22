package edu.cornell.jnutella.identity;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;

import edu.cornell.jnutella.guice.JnutellaScopes;

/**
 * Creates an identity inside of it's identity scope, and populates any scoped objects into the
 * identity scope.
 * 
 * @author Daniel
 */
public class NetworkIdentityFactory {

  private Provider<NetworkIdentity> identityProvider;

  @Inject
  public NetworkIdentityFactory(Provider<NetworkIdentity> identityProvider) {
    this.identityProvider = identityProvider;
  }

  public NetworkIdentity create() {
    Map<String, Object> scopeMap = Maps.newHashMap();
    JnutellaScopes.enterIdentityScope(scopeMap);
    NetworkIdentity identity = identityProvider.get();
    identity.getIdentityScopeMap().putAll(scopeMap);
    JnutellaScopes.exitIdentityScope();
    return identity;
  }
}
