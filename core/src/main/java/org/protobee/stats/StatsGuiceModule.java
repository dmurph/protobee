package org.protobee.stats;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class StatsGuiceModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(DropLog.class).to(LoggingDropLog.class).in(Singleton.class);
  }
}
