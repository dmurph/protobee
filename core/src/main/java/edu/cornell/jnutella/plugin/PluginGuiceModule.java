package edu.cornell.jnutella.plugin;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

import edu.cornell.jnutella.gnutella.Gnutella;
import edu.cornell.jnutella.guice.SessionScope;
import edu.cornell.jnutella.modules.ProtocolModule;
import edu.cornell.jnutella.protocol.ProtocolConfig;

/**
 * Module that plugin modules should subclass. Use the {@link OverridingModule} annotation to
 * signify that you with this module to be part of the overriding modules set.
 * 
 * @author Daniel
 */
public abstract class PluginGuiceModule extends AbstractModule {

  private Multibinder<ProtocolConfig> configBinder = null;
  private Multibinder<ProtocolModule> gnutellaModules = null;

  public Multibinder<ProtocolConfig> getConfigBinder() {
    if (configBinder == null) {
      configBinder = Multibinder.newSetBinder(binder(), ProtocolConfig.class);
    }
    return configBinder;
  }
  
  public Multibinder<ProtocolModule> getGnutellaModulesBinder() {
    if(gnutellaModules == null) {
      gnutellaModules = Multibinder.newSetBinder(binder(), ProtocolModule.class, Gnutella.class);
    }
    return gnutellaModules;
  }

  public void addProtocolConfig(Class<? extends ProtocolConfig>... classes) {
    for (Class<? extends ProtocolConfig> klass : classes) {
      getConfigBinder().addBinding().to(klass).in(Singleton.class);
    }
  }
  
  public void addGnutellaProtocolModuleInSessionScope(Class<? extends ProtocolModule>... classes) {
    for (Class<? extends ProtocolModule> klass : classes) {
      getGnutellaModulesBinder().addBinding().to(klass).in(SessionScope.class);
    }
  }
  
  public <T extends ProtocolModule> void addGnutellaProtocolModuleNoScope(Class<T>... classes) {
    for (Class<T> klass : classes) {
      getGnutellaModulesBinder().addBinding().to(klass);
    }
  }
  
  @VisibleForTesting
  public <T extends ProtocolConfig> void addProtocolConfig(T... configs) {
    for (T config : configs) {
      getConfigBinder().addBinding().toInstance(config);      
    }
  }
  
  
}
