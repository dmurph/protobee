package edu.cornell.jnutella.guice;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.jboss.netty.handler.execution.OrderedDownstreamThreadPoolExecutor;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapMaker;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

import edu.cornell.jnutella.gnutella.GnutellaGuiceModule;
import edu.cornell.jnutella.guice.netty.ExecutorModule;
import edu.cornell.jnutella.identity.NetworkIdentityManager;
import edu.cornell.jnutella.network.NetworkGuiceModule;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.protocol.ProtocolConfig;
import edu.cornell.jnutella.protocol.ProtocolGuiceModule;
import edu.cornell.jnutella.session.SessionGuiceModule;
import edu.cornell.jnutella.util.UtilGuiceModule;

public class JnutellaMainModule extends AbstractModule {

  public static final String USER_AGENT_STRING = "Jnutella/0.1";
  
  @Override
  protected void configure() {
    install(new NetworkGuiceModule());
    install(new UtilGuiceModule());
    install(new LogModule());
    install(new ProtocolGuiceModule());
    install(new SessionGuiceModule());
    install(new GnutellaGuiceModule());
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

    bindScope(SessionScope.class, JnutellaScopes.SESSION);
    bindScope(IdentityScope.class, JnutellaScopes.IDENTITY);
    
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
