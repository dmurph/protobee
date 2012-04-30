package org.protobee.identity;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class IdentityGuiceModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(IdentityTagManager.class).in(Singleton.class);
  }
}
