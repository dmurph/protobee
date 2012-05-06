package org.protobee.plugin;

import org.protobee.gnutella.Gnutella;
import org.protobee.gnutella.messages.GnutellaMessage;
import org.protobee.guice.scopes.SessionScope;
import org.protobee.modules.ProtocolModule;
import org.protobee.util.PreFilter;

import com.google.inject.TypeLiteral;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.multibindings.Multibinder;

public abstract class GnutellaPluginGuiceModule extends PluginGuiceModule {

  private Multibinder<ProtocolModule> gnutellaModules = null;
  private Multibinder<Class<? extends ProtocolModule>> gnutellaModuleClasses = null;
  private Multibinder<PreFilter<GnutellaMessage>> prefilter = null;

  public Multibinder<ProtocolModule> getGnutellaModulesBinder() {
    if (gnutellaModules == null) {
      gnutellaModules = Multibinder.newSetBinder(binder(), ProtocolModule.class, Gnutella.class);
    }
    return gnutellaModules;
  }

  public Multibinder<Class<? extends ProtocolModule>> getGnutellaModuleClassesBinder() {
    if (gnutellaModules == null) {
      gnutellaModuleClasses =
          Multibinder.newSetBinder(binder(), new TypeLiteral<Class<? extends ProtocolModule>>() {},
              Gnutella.class);
    }
    return gnutellaModuleClasses;
  }

  public Multibinder<PreFilter<GnutellaMessage>> getPrefilterBinder() {
    if (prefilter == null) {
      prefilter =
          Multibinder.newSetBinder(binder(), new TypeLiteral<PreFilter<GnutellaMessage>>() {},
              Gnutella.class);
    }
    return prefilter;
  }

  public void addGnutellaModuleInSessionScope(Class<? extends ProtocolModule> klass) {
    getGnutellaModuleClassesBinder().addBinding().toInstance(klass);
    getGnutellaModulesBinder().addBinding().to(klass).in(SessionScope.class);
  }

  public <T extends ProtocolModule> ScopedBindingBuilder addGnutellaModule(Class<T> klass) {
    getGnutellaModuleClassesBinder().addBinding().toInstance(klass);
    return getGnutellaModulesBinder().addBinding().to(klass);
  }

  public <T extends PreFilter<GnutellaMessage>> ScopedBindingBuilder addPreFilter(Class<T> klass) {
    return getPrefilterBinder().addBinding().to(klass);
  }
}
