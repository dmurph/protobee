package org.protobee.plugin;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.protobee.modules.ProtocolModule;
import org.protobee.protocol.ProtocolConfig;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.multibindings.Multibinder;

/**
 * Module that plugin modules should subclass. Use the {@link OverridingModule} annotation to
 * signify that you with this module to be part of the overriding modules set.
 * 
 * @author Daniel
 */
public abstract class PluginGuiceModule extends AbstractModule {

  private Multibinder<ProtocolConfig> configBinder = null;

  private final Map<Class<? extends Annotation>, Multibinder<ProtocolModule>> moduleBinders = Maps
      .newHashMap();

  private final Map<Class<? extends Annotation>, Multibinder<Class<? extends ProtocolModule>>> moduleClassBinders =
      Maps.newHashMap();

  public Multibinder<ProtocolConfig> getConfigBinder() {
    if (configBinder == null) {
      configBinder = Multibinder.newSetBinder(binder(), ProtocolConfig.class);
    }
    return configBinder;
  }

  public Multibinder<ProtocolModule> getModuleMultibinder(
      Class<? extends Annotation> annotationClass) {
    if (!moduleBinders.containsKey(annotationClass)) {
      moduleBinders.put(annotationClass,
          Multibinder.newSetBinder(binder(), ProtocolModule.class, annotationClass));
    }
    return moduleBinders.get(annotationClass);
  }

  public Multibinder<Class<? extends ProtocolModule>> getModuleClassMultibinder(
      Class<? extends Annotation> annotationClass) {
    if (!moduleClassBinders.containsKey(annotationClass)) {
      TypeLiteral<Class<? extends ProtocolModule>> type =
          new TypeLiteral<Class<? extends ProtocolModule>>() {};
      moduleClassBinders.put(annotationClass,
          Multibinder.newSetBinder(binder(), type, annotationClass));
    }
    return moduleClassBinders.get(annotationClass);
  }

  public void addProtocolConfig(Class<? extends ProtocolConfig> klass,
      Class<? extends Annotation> configAnnotation) {
    bind(ProtocolConfig.class).annotatedWith(configAnnotation).to(klass).in(Singleton.class);
    getConfigBinder().addBinding().to(Key.get(ProtocolConfig.class, configAnnotation)).in(Singleton.class);
  }

  public ScopedBindingBuilder addModuleBinding(Class<? extends ProtocolModule> moduleClass,
      Class<? extends Annotation> annotationClass) {
    getModuleClassMultibinder(annotationClass).addBinding().toInstance(moduleClass);
    return getModuleMultibinder(annotationClass).addBinding().to(moduleClass);
  }

  @VisibleForTesting
  public <T extends ProtocolConfig> void addProtocolConfig(T config) {
    getConfigBinder().addBinding().toInstance(config);
  }
}
