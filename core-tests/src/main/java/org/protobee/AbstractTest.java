package org.protobee;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.handler.codec.http.HttpMessageDecoder;
import org.jboss.netty.handler.codec.http.HttpMessageEncoder;
import org.junit.After;
import org.junit.Before;
import org.protobee.guice.scopes.ProtobeeScopes;
import org.protobee.identity.NetworkIdentity;
import org.protobee.identity.NetworkIdentityFactory;
import org.protobee.identity.NetworkIdentityManager;
import org.protobee.modules.ProtocolModule;
import org.protobee.netty.LocalNettyTester;
import org.protobee.plugin.PluginGuiceModule;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.ProtocolConfig;
import org.protobee.protocol.ProtocolModel;
import org.protobee.session.SessionModel;
import org.protobee.session.SessionModelFactory;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.net.InetAddresses;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Modules;


public abstract class AbstractTest {

  protected Injector injector;
  protected Set<LocalNettyTester> localTesters;

  @Before
  public void setup() {
    localTesters = Sets.newHashSet();
    injector = Guice.createInjector(new ProtobeeGuiceModule());
  }

  public LocalNettyTester createLocalNettyTester() {
    LocalNettyTester tester = new LocalNettyTester();
    localTesters.add(tester);
    return tester;
  }

  public Injector getInjector(Module overridingModule) {
    return Guice.createInjector(Modules.override(new ProtobeeGuiceModule()).with(overridingModule));
  }

  public static NetworkIdentity createIdentity(Injector inj) {
    NetworkIdentityFactory factory = inj.getInstance(NetworkIdentityFactory.class);
    return factory.create();
  }

  public Injector getInjectorWithProtocolConfig(ProtocolConfig... configs) {
    return getInjector(getModuleWithProtocolConfig(configs));
  }

  public static AbstractModule getModuleWithProtocolConfig(final ProtocolConfig... configs) {
    return new PluginGuiceModule() {
      @Override
      protected void configure() {
        for (ProtocolConfig protocolConfig : configs) {
          addProtocolConfig(protocolConfig);
        }
      }
    };
  }

  public static ProtocolConfig mockDefaultProtocolConfig() {
    ProtocolConfig config = mock(ProtocolConfig.class);
    when(config.get()).thenReturn(mock(Protocol.class));

    when(config.createProtocolModules()).thenReturn(Sets.<ProtocolModule>newHashSet());
    when(config.getModuleClasses()).thenReturn(Sets.<Class<? extends ProtocolModule>>newHashSet());

    when(config.createRequestDecoder()).thenReturn(mock(HttpMessageDecoder.class));
    when(config.createRequestEncoder()).thenReturn(mock(HttpMessageEncoder.class));
    when(config.getServerOptions()).thenReturn(Maps.<String, Object>newHashMap());
    when(config.getConnectionOptions()).thenReturn(Maps.<String, Object>newHashMap());
    when(config.getListeningAddress()).thenReturn(null);
    return config;
  }

  public static ProtocolConfig mockDefaultProtocolConfig(Set<ProtocolModule> sessionModules) {
    ProtocolConfig config = mock(ProtocolConfig.class);
    when(config.get()).thenReturn(mock(Protocol.class));

    when(config.createProtocolModules()).thenReturn(sessionModules);
    when(config.getModuleClasses()).thenReturn(
        Sets.newHashSet(Iterables.transform(sessionModules,
            new Function<ProtocolModule, Class<? extends ProtocolModule>>() {
              @Override
              public Class<? extends ProtocolModule> apply(ProtocolModule input) {
                return input.getClass();
              }
            })));

    when(config.createRequestDecoder()).thenReturn(mock(HttpMessageDecoder.class));
    when(config.createRequestEncoder()).thenReturn(mock(HttpMessageEncoder.class));
    when(config.getServerOptions()).thenReturn(Maps.<String, Object>newHashMap());
    when(config.getConnectionOptions()).thenReturn(Maps.<String, Object>newHashMap());
    when(config.getListeningAddress()).thenReturn(null);
    return config;
  }

  public static InetSocketAddress createAddress(String ip, int port) {
    return new InetSocketAddress(InetAddresses.forString(ip), port);
  }

  public static Set<ProtocolModel> fromConfigs(Injector inj, Set<ProtocolConfig> configs) {
    Map<Protocol, ProtocolModel> map = getProtocolToModelMap(inj);
    Set<ProtocolModel> models = Sets.newHashSet();

    for (ProtocolConfig config : configs) {
      models.add(map.get(config.get()));
    }
    return models;
  }

  public static ProtocolModel fromConfig(Injector inj, ProtocolConfig config) {
    return getModelFor(inj, config.get());
  }

  public static Map<Protocol, ProtocolModel> getProtocolToModelMap(Injector inj) {
    return inj.getInstance(Key.get(new TypeLiteral<Map<Protocol, ProtocolModel>>() {}));
  }

  public static ProtocolModel getModelFor(Injector inj, Protocol protocol) {
    return getProtocolToModelMap(inj).get(protocol);
  }

  public static SessionModel createSession(Injector inj, SocketAddress address,
      ProtocolConfig protocol) {
    NetworkIdentityManager manager = inj.getInstance(NetworkIdentityManager.class);
    NetworkIdentity identity = manager.getNetworkIdentityWithNewConnection(protocol.get(), address);

    return createSession(identity, inj, protocol);

  }

  public static SessionModel createSession(NetworkIdentity identity, Injector inj,
      ProtocolConfig protocol) {

    ProtocolModel model = getModelFor(inj, protocol.get());
    model.enterScope();
    identity.enterScope();
    SessionModelFactory sessionFactory = inj.getInstance(SessionModelFactory.class);
    SessionModel session = sessionFactory.create("AbstractTest#createSession()");
    identity.exitScope();
    model.exitScope();
    identity.registerNewSession(protocol.get(), session);
    return session;
  }

  @After
  public void freeScopes() {
    for (LocalNettyTester tester : localTesters) {
      tester.shutdown(1000);
    }
    ProtobeeScopes.IDENTITY.exitScope();
    ProtobeeScopes.PROTOCOL.exitScope();
    ProtobeeScopes.SESSION.exitScope();
  }
}
