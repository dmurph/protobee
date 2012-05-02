package org.protobee.plugin;

import org.protobee.gnutella.Gnutella;
import org.protobee.gnutella.filters.GnutellaPreFilter;
import org.protobee.guice.scopes.SessionScope;
import org.protobee.modules.ProtocolModule;
import org.protobee.protocol.ProtocolConfig;

import com.google.inject.Singleton;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.multibindings.Multibinder;


/**
 * Module that plugin modules should subclass. Use the {@link OverridingModule} annotation to
 * signify that you with this module to be part of the overriding modules set.
 * 
 * @author Daniel
 */
public abstract class GnutellaPluginGuiceModule extends PluginGuiceModule {

  private Multibinder<ProtocolModule> gnutellaModules = null;
  private Multibinder<GnutellaPreFilter> prefilter = null;
  
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
}
