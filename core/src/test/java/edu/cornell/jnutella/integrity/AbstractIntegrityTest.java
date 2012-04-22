package edu.cornell.jnutella.integrity;

import org.junit.Before;

import com.google.inject.Guice;
import com.google.inject.Injector;

import edu.cornell.jnutella.Jnutella;

public class AbstractIntegrityTest {

  protected Injector injector;

  @Before
  public void setup() {
    injector = Guice.createInjector(Jnutella.getCombinedModules());
  }
}
