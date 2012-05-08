package org.protobee.protocol;

import java.lang.annotation.Annotation;
import java.net.SocketAddress;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.DefaultChannelFuture;
import org.jboss.netty.handler.codec.http.HttpMessageDecoder;
import org.jboss.netty.handler.codec.http.HttpMessageEncoder;
import org.protobee.compatability.ModuleCompatabilityVersionMerger;
import org.protobee.guice.scopes.PrescopedProvider;
import org.protobee.guice.scopes.ProtocolScope;
import org.protobee.guice.scopes.SessionScope;
import org.protobee.modules.ProtocolModule;
import org.protobee.plugin.ConfigAnnotation;
import org.protobee.protocol.handlers.DownstreamMessagePosterHandler;
import org.protobee.protocol.handlers.UpstreamMessagePosterHandler;
import org.protobee.util.ProtocolConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class ProtocolGuiceModule extends AbstractModule {

  private final Logger log = LoggerFactory.getLogger(ProtocolGuiceModule.class);


  @Override
  protected void configure() {
    install(new FactoryModuleBuilder().build(UpstreamMessagePosterHandler.Factory.class));
    install(new FactoryModuleBuilder().build(DownstreamMessagePosterHandler.Factory.class));

    bind(ModuleCompatabilityVersionMerger.class).in(ProtocolScope.class);

    bind(ProtocolConfig.class).toProvider(new PrescopedProvider<ProtocolConfig>("ProtocolConfig"))
        .in(ProtocolScope.class);

    bind(ProtocolModel.class).in(ProtocolScope.class);

    TypeLiteral<Set<ProtocolModel>> protocolScopes = new TypeLiteral<Set<ProtocolModel>>() {};
    bind(protocolScopes).toProvider(ProtocolModelsProvider.class).in(Singleton.class);
  }

  @Provides
  @ProtocolScope
  @ConfigAnnotation
  public Class<? extends Annotation> getConfigAnnotation(ProtocolConfig config,
      Map<Class<? extends ProtocolConfig>, Class<? extends Annotation>> configToAnnotationMap) {
    return configToAnnotationMap.get(config.getClass());
  }



  @Provides
  @HandshakeFuture
  @SessionScope
  public ChannelFuture getHandshakeFuture(Channel channel) {
    log.info("Creating handshake future for channel " + channel);
    return new DefaultChannelFuture(channel, false);
  }

  @Provides
  @Singleton
  public Map<Protocol, ProtocolConfig> getConfigMap(Set<ProtocolConfig> protocols) {
    ImmutableMap.Builder<Protocol, ProtocolConfig> builder = ImmutableMap.builder();
    for (ProtocolConfig protocolConfig : protocols) {
      Protocol protocol = protocolConfig.get();
      Preconditions.checkNotNull(protocol, "Protocol is null for config " + protocolConfig);
      builder.put(protocol, protocolConfig);
    }
    return builder.build();
  }

  @Provides
  @Singleton
  public Map<Protocol, ProtocolModel> getModelMap(Set<ProtocolModel> models) {
    ImmutableMap.Builder<Protocol, ProtocolModel> builder = ImmutableMap.builder();
    for (ProtocolModel model : models) {
      Protocol protocol = model.getProtocol();
      Preconditions.checkNotNull(protocol, "Protocol is null for model " + model);
      builder.put(protocol, model);
    }
    return builder.build();
  }

  @Provides
  @Singleton
  public Set<Protocol> getProtocols(Set<ProtocolModel> configs) {
    return ProtocolConfigUtils.getProtocolSetFromModels(configs);
  }


  // protocol model providers
  @Provides
  @ProtocolScope
  public Protocol getProtocol(ProtocolModel model) {
    return model.getProtocol();
  }

  @Provides
  @ProtocolScope
  public Set<Class<? extends ProtocolModule>> getModuleClasses(ProtocolModel model) {
    return model.getModuleClasses();
  }

  @Provides
  @ProtocolScope
  @ServerOptionsMap
  public Map<String, Object> getServerOptions(ProtocolModel model) {
    return model.getServerOptions();
  }

  @Provides
  @ProtocolScope
  @ConnectionOptionsMap
  public Map<String, Object> getConnectionOptions(ProtocolModel model) {
    return model.getConnectionOptions();
  }

  @Provides
  @LocalListeningAddress
  @ProtocolScope
  public SocketAddress getLocalAddress(ProtocolModel model) {
    return model.getLocalListeningAddress();
  }

  // ////// config bound providers

  @Provides
  @ProtocolChannelHandlers
  @SessionScope
  public ChannelHandler[] createProtocolHandlers(ProtocolConfig config) {
    return config.createProtocolHandlers();
  }

  @Provides
  @RequestEncoding
  @SessionScope
  public HttpMessageEncoder createEncoder(ProtocolConfig config) {
    return config.createRequestEncoder();
  }

  @Provides
  @RequestEncoding
  @SessionScope
  public HttpMessageDecoder createDecoder(ProtocolConfig config) {
    return config.createRequestDecoder();
  }

  @Provides
  @SessionScope
  public Set<ProtocolModule> createModules(ProtocolConfig config) {
    return config.createProtocolModules();
  }
}
