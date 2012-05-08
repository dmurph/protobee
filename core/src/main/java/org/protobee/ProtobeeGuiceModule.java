package org.protobee;

import java.lang.annotation.Annotation;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.jboss.netty.handler.execution.OrderedDownstreamThreadPoolExecutor;
import org.jboss.netty.logging.InternalLoggerFactory;
import org.jboss.netty.logging.Slf4JLoggerFactory;
import org.protobee.annotation.UserAgent;
import org.protobee.guice.LogModule;
import org.protobee.guice.netty.ExecutorModule;
import org.protobee.guice.netty.NettyGuiceModule;
import org.protobee.guice.scopes.ScopesGuiceModule;
import org.protobee.identity.NetworkIdentityManager;
import org.protobee.network.NetworkGuiceModule;
import org.protobee.protocol.ProtocolConfig;
import org.protobee.protocol.ProtocolGuiceModule;
import org.protobee.session.SessionGuiceModule;
import org.protobee.stats.StatsGuiceModule;
import org.protobee.util.UtilGuiceModule;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;

public class ProtobeeGuiceModule extends AbstractModule {

  public static final String USER_AGENT_STRING = "Jnutella/0.1";

  @Override
  protected void configure() {
    install(new ScopesGuiceModule());
    install(new NetworkGuiceModule());
    install(new UtilGuiceModule());
    install(new LogModule());
    install(new ProtocolGuiceModule());
    install(new StatsGuiceModule());
    install(new SessionGuiceModule());
    install(new NettyGuiceModule());

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

    TypeLiteral<Class<? extends ProtocolConfig>> keyType =
        new TypeLiteral<Class<? extends ProtocolConfig>>() {};
    TypeLiteral<Class<? extends Annotation>> valueType =
        new TypeLiteral<Class<? extends Annotation>>() {};
    MapBinder.newMapBinder(binder(), keyType, valueType);

    InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());

    bind(NetworkIdentityManager.class).in(Singleton.class);

    bind(ProtobeeServantBootstrapper.class).in(Singleton.class);

    bindConstant().annotatedWith(UserAgent.class).to("Jnutella/0.1");
  }
}
