package org.protobee.integrity;

import org.junit.Before;
import org.protobee.guice.JnutellaModuleCombiner;

import com.google.inject.Guice;
import com.google.inject.Injector;


public class AbstractIntegrityTest {

  protected Injector injector;

  @Before
  public void setup() {
    injector = Guice.createInjector(JnutellaModuleCombiner.getCombinedModules());
  }
}
