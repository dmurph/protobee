package edu.cornell.jnutella;

import java.util.concurrent.Executors;

import org.junit.Before;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

import edu.cornell.jnutella.guice.JnutellaGuiceModule;
import edu.cornell.jnutella.guice.netty.ExecutorModule;

public abstract class AbstractTest {

  protected Injector injector;

  @Before
  public void setup() {
    injector =
        Guice.createInjector(new JnutellaGuiceModule(),
            new ExecutorModule(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
  }

  public Injector getInjector(Module overridingModule) {
    return Guice.createInjector(Modules.override(new JnutellaGuiceModule(),
        new ExecutorModule(Executors.newCachedThreadPool(), Executors.newCachedThreadPool())).with(
        overridingModule));
  }
}
