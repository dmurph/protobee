package org.protobee;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Set;

import org.jboss.netty.handler.codec.http.HttpMessageDecoder;
import org.jboss.netty.handler.codec.http.HttpMessageEncoder;
import org.junit.After;
import org.junit.Before;
import org.protobee.guice.ProtobeeScopes;
import org.protobee.identity.NetworkIdentity;
import org.protobee.identity.NetworkIdentityFactory;
import org.protobee.identity.NetworkIdentityManager;
import org.protobee.modules.ProtocolModule;
import org.protobee.plugin.PluginGuiceModule;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.ProtocolConfig;
import org.protobee.session.ProtocolModulesHolder;
import org.protobee.session.SessionModel;
import org.protobee.session.SessionModelFactory;

import com.google.common.collect.Maps;
import com.google.common.net.InetAddresses;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;


public abstract class AbstractTest {

  protected Injector injector;

  @Before
  public void setup() {
    injector = Guice.createInjector(new ProtobeeGuiceModule());
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
    when(config.createSessionModel()).thenReturn(mock(ProtocolModulesHolder.class));
    when(config.createRequestDecoder()).thenReturn(mock(HttpMessageDecoder.class));
    when(config.createRequestEncoder()).thenReturn(mock(HttpMessageEncoder.class));
    when(config.getServerBootstrapOptions()).thenReturn(Maps.<String, Object>newHashMap());
    return config;
  }

  public static ProtocolConfig mockDefaultProtocolConfig(Set<ProtocolModule> sessionModules) {

    ProtocolModulesHolder sessionModel = mock(ProtocolModulesHolder.class);
    when(sessionModel.getMutableModules()).thenReturn(sessionModules);

    ProtocolConfig config = mock(ProtocolConfig.class);
    when(config.get()).thenReturn(mock(Protocol.class));
    when(config.createSessionModel()).thenReturn(sessionModel);
    when(config.createRequestDecoder()).thenReturn(mock(HttpMessageDecoder.class));
    when(config.createRequestEncoder()).thenReturn(mock(HttpMessageEncoder.class));
    when(config.getServerBootstrapOptions()).thenReturn(Maps.<String, Object>newHashMap());
    return config;
  }

  public static InetSocketAddress createAddress(String ip, int port) {
    return new InetSocketAddress(InetAddresses.forString(ip), port);
  }

  public static SessionModel createSession(Injector injector, SocketAddress address,
      ProtocolConfig protocol) {
    NetworkIdentityManager manager = injector.getInstance(NetworkIdentityManager.class);
    NetworkIdentity identity = manager.getNetworkIdentityWithNewConnection(protocol.get(), address);
    identity.enterScope();
    SessionModelFactory sessionFactory = injector.getInstance(SessionModelFactory.class);
    SessionModel session = sessionFactory.create(protocol, "AbstractTest#createSession()");
    identity.exitScope();
    identity.registerNewSession(protocol.get(), session);
    return session;
  }

  public static SessionModel createSession(NetworkIdentity identity, Injector injector,
      ProtocolConfig protocol) {
    identity.enterScope();
    SessionModelFactory sessionFactory = injector.getInstance(SessionModelFactory.class);
    SessionModel session = sessionFactory.create(protocol, "AbstractTest#createSession()");
    identity.exitScope();
    identity.registerNewSession(protocol.get(), session);
    return session;
  }

  @After
  public void freeScopes() {
    ProtobeeScopes.exitIdentityScope();
    ProtobeeScopes.exitSessionScope();
  }
}
