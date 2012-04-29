package org.protobee.identity;

import java.util.Map;

import org.protobee.guice.JnutellaScopes;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Provider;


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
