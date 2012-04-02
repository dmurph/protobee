package edu.cornell.jnutella.guice;

import java.util.Set;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;

import edu.cornell.jnutella.session.gnutella.ForMessageTypes;

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


  public interface CreationFactory {
    Valued create(String value);
  }

  public static class AssistedValue1 implements Valued {

    private String value;

    @Inject
    public AssistedValue1(@Assisted String value) {
      this.value = value;
    }

    @Override
    public String getValue() {
      return value;
    }
  }

  public static class AssistedValue2 implements Valued {

    private String value;

    @Inject
    public AssistedValue2(@Assisted String value) {
      this.value = value;
    }

    @Override
    public String getValue() {
      return value + "2";
    }
  }
  

  @Test
  public void testInstanceFromAnnotation() {
    AbstractModule module = new AbstractModule() {

      @Override
      protected void configure() {
        install(new FactoryModuleBuilder()
            .implement(Key.get(Valued.class, Names.named("1")), AssistedValue1.class)
            .implement(Key.get(Valued.class, Names.named("2")), AssistedValue2.class)
            .build(CreationFactory.class));
      }
    };
    
    Injector injector = Guice.createInjector(module);
    CreationFactory factory = injector.getInstance(Key.get(CreationFactory.class, Names.named("1")));
    assertEquals("h", factory.create("h").getValue());
  }
}
