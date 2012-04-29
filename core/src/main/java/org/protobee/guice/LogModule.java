package org.protobee.guice;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

public class LogModule extends AbstractModule {

  @Override
  protected void configure() {
    bindListener(Matchers.any(), new Slf4jTypeListener());
  }

}
