package edu.cornell.jnutella.plugin;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

import edu.cornell.jnutella.protocol.ProtocolConfig;

/**
 * Module that plugin modules should subclass. Use the {@link OverridingModule} annotation to
 * signify that you with this module to be part of the overriding modules set.
 * 
 * @author Daniel
 */
public abstract class PluginGuiceModule extends AbstractModule {

  private Multibinder<ProtocolConfig> configBinder = null;

  public Multibinder<ProtocolConfig> getConfigBinder() {
    if (configBinder == null) {
      configBinder = Multibinder.newSetBinder(binder(), ProtocolConfig.class);
    }
    return configBinder;
  }

  public <T extends ProtocolConfig> void addProtocolConfig(Class<T>... classes) {
    for (Class<T> klass : classes) {
      getConfigBinder().addBinding().to(klass).in(Singleton.class);
    }
  }
  
  @VisibleForTesting
  public <T extends ProtocolConfig> void addProtocolConfig(T... configs) {
    for (T config : configs) {
      getConfigBinder().addBinding().toInstance(config);      
    }
  }
}
