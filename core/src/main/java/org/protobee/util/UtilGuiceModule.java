package org.protobee.util;

import org.protobee.guice.scopes.ProtocolScope;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class UtilGuiceModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(Clock.class).to(SystemClock.class).in(Singleton.class);
    bind(ProtocolConfigUtils.class).in(ProtocolScope.class);
  }
}
