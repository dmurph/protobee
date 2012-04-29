package org.protobee.guice;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.Test;
import org.protobee.gnutella.session.ForMessageTypes;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.util.Modules;


public class GuiceTests {

  public static interface Valued {
    String getValue();
  }

  public static class OriginalValue implements Valued {
    @Override
    public String getValue() {
      return "YO_ME";
    }
  }
  public static class PluginValue implements Valued {
    @Override
    public String getValue() {
      return "YO_ME2";
    }
  }

  public static class InjectionTestClass {

    Set<Valued> yos;

    @Inject
    public InjectionTestClass(Set<Valued> yoos) {
      this.yos = yoos;
    }
  }

  @Test
  public void testMultibindingWithAnnotation() {
    AbstractModule module = new AbstractModule() {

      @Override
      protected void configure() {
        bind(Valued.class).annotatedWith(ForMessageTypes.with((byte) 0x01)).to(OriginalValue.class);

        Multibinder<Valued> mb = Multibinder.newSetBinder(binder(), Valued.class);

        mb.addBinding().to(Key.get(Valued.class, ForMessageTypes.with((byte) 0x01)));
      }
    };

    Injector injector = Guice.createInjector(module);
    Valued yo = injector.getInstance(Key.get(Valued.class, ForMessageTypes.with((byte) 0x01)));
    assertEquals("YO_ME", yo.getValue());

    InjectionTestClass test = injector.getInstance(InjectionTestClass.class);
    assertEquals("YO_ME", test.yos.iterator().next().getValue());

    AbstractModule plugin = new AbstractModule() {

      @Override
      protected void configure() {
        bind(Valued.class).annotatedWith(ForMessageTypes.with((byte) 0x01)).to(PluginValue.class);
      }
    };

    injector = Guice.createInjector(Modules.override(module).with(plugin));
    yo = injector.getInstance(Key.get(Valued.class, ForMessageTypes.with((byte) 0x01)));
    assertEquals("YO_ME2", yo.getValue());

    test = injector.getInstance(InjectionTestClass.class);
    assertEquals("YO_ME2", test.yos.iterator().next().getValue());
  }

  private static int numConstructed;

  public static class InjClass implements Valued {
    public InjClass() {
      GuiceTests.numConstructed++;
    }

    @Override
    public String getValue() {
      return null;
    }
  }


  @Test
  public void testMultipleMultibinding() {
    AbstractModule module = new AbstractModule() {

      @Override
      protected void configure() {

        Multibinder<Valued> mb = Multibinder.newSetBinder(binder(), Valued.class);
        mb.addBinding().to(InjClass.class).in(Singleton.class);
      }
    };
    Injector injector = Guice.createInjector(module);

    numConstructed = 0;
    injector.getInstance(MultiClass.class);
    injector.getInstance(MultiClass.class);

    assertEquals(1, numConstructed);
  }

  public static class MultiClass {
    @Inject
    public MultiClass(Set<Valued> valued) {

    }
  }
}
