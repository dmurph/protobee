package org.protobee.plugin;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.protobee.modules.ProtocolModule;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.ProtocolConfig;
import org.protobee.protocol.ProtocolModel;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.multibindings.MapBinder;
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


  private MapBinder<Class<? extends ProtocolConfig>, Class<? extends Annotation>> annotation = null;

  // private MapBinder<Class<? extends ProtocolConfig>, Set<ProtocolModule>> modules = null;
  // private MapBinder<Class<? extends ProtocolConfig>, Set<Class<? extends ProtocolModule>>>
  // moduleClasses =
  // null;
  // private MapBinder<Class<? extends ProtocolConfig>, Map<String, Object>> serverOptions = null;
  // private MapBinder<Class<? extends ProtocolConfig>, Map<String, Object>> connectionOptions =
  // null;
  // private MapBinder<Class<? extends ProtocolConfig>, SocketAddress> localAddress = null;
  // private MapBinder<Class<? extends ProtocolConfig>, ChannelHandler[]> channelHandlers = null;
  // private MapBinder<Class<? extends ProtocolConfig>, HttpMessageEncoder> requestEncoder = null;
  // private MapBinder<Class<? extends ProtocolConfig>, HttpMessageDecoder> requestDecoder = null;

  private MapBinder<Class<? extends ProtocolConfig>, Class<? extends Annotation>> getAnnotation() {
    if (annotation == null) {
      TypeLiteral<Class<? extends ProtocolConfig>> keyType =
          new TypeLiteral<Class<? extends ProtocolConfig>>() {};
      TypeLiteral<Class<? extends Annotation>> valueType =
          new TypeLiteral<Class<? extends Annotation>>() {};
      annotation = MapBinder.newMapBinder(binder(), keyType, valueType);
    }
    return annotation;
  }

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
      final Class<? extends Annotation> configAnnotation) {

    // bind config
    bind(ProtocolConfig.class).annotatedWith(configAnnotation).to(klass).in(Singleton.class);
    // add to config set
    getConfigBinder().addBinding().to(Key.get(ProtocolConfig.class, configAnnotation))
        .in(Singleton.class);
    // bind protocol
    bind(Protocol.class).annotatedWith(configAnnotation)
        .toProvider(Key.get(ProtocolConfig.class, configAnnotation)).in(Singleton.class);

    // bind our config annotation
    getAnnotation().addBinding(klass).toInstance(configAnnotation);

    // protocol model
    TypeLiteral<Map<Protocol, ProtocolModel>> mapType =
        new TypeLiteral<Map<Protocol, ProtocolModel>>() {};
    final Provider<Map<Protocol, ProtocolModel>> mapProvider =
        binder().getProvider(Key.get(mapType));
    final Provider<Protocol> protocolProvider =
        binder().getProvider(Key.get(Protocol.class, configAnnotation));
    bind(ProtocolModel.class).annotatedWith(configAnnotation)
        .toProvider(new Provider<ProtocolModel>() {
          @Override
          public ProtocolModel get() {
            return mapProvider.get().get(protocolProvider.get());
          }

          @Override
          public String toString() {
            return configAnnotation + "-ProtocolModelProvider";
          }
        }).in(Singleton.class);

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
