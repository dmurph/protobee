package org.protobee.util;

import org.protobee.guice.ProtobeeScopes;
import org.protobee.identity.NetworkIdentity;
import org.protobee.session.SessionModel;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;


/**
 * Utility class for unscoping + rescoping any current identity or session scope
 * 
 * @author Daniel
 */
public class Descoper {

  private final Provider<NetworkIdentity> networkIdentityProvider;
  private final Provider<SessionModel> sessionModelProvider;

  private NetworkIdentity unscopedIdentity = null;
  private SessionModel unscopedModel = null;

  @Inject
  public Descoper(Provider<NetworkIdentity> networkIdentityProvider,
      Provider<SessionModel> sessionModelProvider) {
    this.networkIdentityProvider = networkIdentityProvider;
    this.sessionModelProvider = sessionModelProvider;
  }

  public void descope() {
    Preconditions.checkState(unscopedIdentity == null, "Must rescope before unscoping again");
    Preconditions.checkState(unscopedModel == null, "Must rescope before unscoping again");
    if (ProtobeeScopes.isInIdentityScope()) {
      unscopedIdentity = networkIdentityProvider.get();
      unscopedIdentity.exitScope();
    }
    if (ProtobeeScopes.isInSessionScope()) {
      unscopedModel = sessionModelProvider.get();
      unscopedModel.exitScope();
    }
  }

  public void rescope() {
    if (unscopedIdentity != null) {
      unscopedIdentity.enterScope();
      unscopedIdentity = null;
    }

    if (unscopedModel != null) {
      unscopedModel.enterScope();
      unscopedModel = null;
    }
  }
}
