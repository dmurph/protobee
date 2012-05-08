package org.protobee.identity;

import org.protobee.guice.scopes.IdentityScope;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class IdentityGuiceModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(IdentityTagManager.class).in(Singleton.class);
    bind(NetworkIdentityManager.class).in(Singleton.class);
    bind(NetworkIdentityFactory.class).in(Singleton.class);
    bind(NetworkIdentity.class).in(IdentityScope.class);
  }

  @Provides
  @Me
  @Singleton
  public NetworkIdentity getMe(NetworkIdentityManager manager) {
    return manager.getMe();
  }
}
