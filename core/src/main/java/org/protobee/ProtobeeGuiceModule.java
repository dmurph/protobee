package org.protobee;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.jboss.netty.handler.execution.OrderedDownstreamThreadPoolExecutor;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;
import org.protobee.annotation.UserAgent;
import org.protobee.guice.IdentityScope;
import org.protobee.guice.IdentityScopeMap;
import org.protobee.guice.ProtobeeScopes;
import org.protobee.guice.LogModule;
import org.protobee.guice.SessionScope;
import org.protobee.guice.SessionScopeMap;
import org.protobee.guice.netty.ExecutorModule;
import org.protobee.identity.NetworkIdentityManager;
import org.protobee.network.NetworkGuiceModule;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.ProtocolConfig;
import org.protobee.protocol.ProtocolGuiceModule;
import org.protobee.session.SessionGuiceModule;
import org.protobee.stats.StatsGuiceModule;
import org.protobee.util.ProtocolConfigUtils;
import org.protobee.util.UtilGuiceModule;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapMaker;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;


public class ProtobeeGuiceModule extends AbstractModule {

  public static final String USER_AGENT_STRING = "Jnutella/0.1";
  
  @Override
  protected void configure() {
    install(new NetworkGuiceModule());
    install(new UtilGuiceModule());
    install(new LogModule());
    install(new ProtocolGuiceModule());
    install(new StatsGuiceModule());
    install(new SessionGuiceModule());
    install(new ExecutorModule(new Provider<Executor>() {
      @Override
      public Executor get() {
        return Executors.newCachedThreadPool();
      }
    }, new Provider<Executor>() {
      @Override
      public Executor get() {
        return new OrderedDownstreamThreadPoolExecutor(10);
      }
    }));

    Multibinder.newSetBinder(binder(), ProtocolConfig.class);

    InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());

    bind(NetworkIdentityManager.class).in(Singleton.class);

    bindScope(SessionScope.class, ProtobeeScopes.SESSION);
    bindScope(IdentityScope.class, ProtobeeScopes.IDENTITY);
    
    bindConstant().annotatedWith(UserAgent.class).to("Jnutella/0.1");
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
  public Set<Protocol> getProtocols(Set<ProtocolConfig> configs) {
    return ProtocolConfigUtils.getProtocolSet(configs);
  }

  @Provides
  @SessionScopeMap
  public Map<String, Object> createSessionScopeMap() {
    return new MapMaker().concurrencyLevel(4).makeMap();
  }

  @Provides
  @IdentityScopeMap
  public Map<String, Object> createIdentiyScopeMap() {
    return new MapMaker().concurrencyLevel(4).makeMap();
  }
}
