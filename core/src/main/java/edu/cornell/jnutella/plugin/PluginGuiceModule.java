package edu.cornell.jnutella.plugin;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.multibindings.Multibinder;

import edu.cornell.jnutella.gnutella.Gnutella;
import edu.cornell.jnutella.gnutella.filters.GnutellaPreFilter;
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
  private Multibinder<GnutellaPreFilter> prefilter = null;

  public Multibinder<ProtocolConfig> getConfigBinder() {
    if (configBinder == null) {
      configBinder = Multibinder.newSetBinder(binder(), ProtocolConfig.class);
    }
    return configBinder;
  }

  public Multibinder<ProtocolModule> getGnutellaModulesBinder() {
    if (gnutellaModules == null) {
      gnutellaModules = Multibinder.newSetBinder(binder(), ProtocolModule.class, Gnutella.class);
    }
    return gnutellaModules;
  }

  public Multibinder<GnutellaPreFilter> getPrefilterBinder() {
    if (prefilter == null) {
      prefilter = Multibinder.newSetBinder(binder(), GnutellaPreFilter.class);
    }
    return prefilter;
  }

  public void addProtocolConfig(Class<? extends ProtocolConfig> klass) {
    getConfigBinder().addBinding().to(klass).in(Singleton.class);
  }

  public void addGnutellaModuleInSessionScope(Class<? extends ProtocolModule> klass) {
    getGnutellaModulesBinder().addBinding().to(klass).in(SessionScope.class);
  }

  public <T extends ProtocolModule> ScopedBindingBuilder addGnutellaModule(Class<T> klass) {
    return getGnutellaModulesBinder().addBinding().to(klass);
  }

  public <T extends GnutellaPreFilter> ScopedBindingBuilder addPreFilter(Class<T> klass) {
    return getPrefilterBinder().addBinding().to(klass);
  }

  @VisibleForTesting
  public <T extends ProtocolConfig> void addProtocolConfig(T config) {
    getConfigBinder().addBinding().toInstance(config);
  }
}
