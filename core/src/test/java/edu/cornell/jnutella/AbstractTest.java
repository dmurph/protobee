package edu.cornell.jnutella;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Set;

import org.jboss.netty.handler.codec.http.HttpMessageDecoder;
import org.jboss.netty.handler.codec.http.HttpMessageEncoder;
import org.junit.After;
import org.junit.Before;

import com.google.common.collect.Maps;
import com.google.common.net.InetAddresses;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.util.Modules;

import edu.cornell.jnutella.gnutella.Gnutella;
import edu.cornell.jnutella.gnutella.GnutellaServantModel;
import edu.cornell.jnutella.guice.JnutellaMainModule;
import edu.cornell.jnutella.guice.JnutellaScopes;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.identity.NetworkIdentityFactory;
import edu.cornell.jnutella.identity.NetworkIdentityManager;
import edu.cornell.jnutella.modules.ProtocolModule;
import edu.cornell.jnutella.plugin.PluginGuiceModule;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.protocol.ProtocolConfig;
import edu.cornell.jnutella.session.ProtocolSessionModel;
import edu.cornell.jnutella.session.SessionModel;
import edu.cornell.jnutella.session.SessionModelFactory;

public abstract class AbstractTest {

  protected Injector injector;

  @Before
  public void setup() {
    injector = Guice.createInjector(new JnutellaMainModule());
  }

  public static Injector getInjector(Module overridingModule) {
    return Guice.createInjector(Modules.override(new JnutellaMainModule()).with(overridingModule));
  }

  public static NetworkIdentity createIdentity(Injector inj) {
    NetworkIdentityFactory factory = inj.getInstance(NetworkIdentityFactory.class);
    return factory.create();
  }

  public static Injector getInjectorWithProtocolConfig(ProtocolConfig... configs) {
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
    when(config.createSessionModel()).thenReturn(mock(ProtocolSessionModel.class));
    when(config.createRequestDecoder()).thenReturn(mock(HttpMessageDecoder.class));
    when(config.createRequestEncoder()).thenReturn(mock(HttpMessageEncoder.class));
    when(config.getNettyBootstrapOptions()).thenReturn(Maps.<String, Object>newHashMap());
    return config;
  }

  public static ProtocolConfig mockDefaultProtocolConfig(Set<ProtocolModule> sessionModules) {

    ProtocolSessionModel sessionModel = mock(ProtocolSessionModel.class);
    when(sessionModel.getMutableModules()).thenReturn(sessionModules);

    ProtocolConfig config = mock(ProtocolConfig.class);
    when(config.get()).thenReturn(mock(Protocol.class));
    when(config.createSessionModel()).thenReturn(sessionModel);
    when(config.createRequestDecoder()).thenReturn(mock(HttpMessageDecoder.class));
    when(config.createRequestEncoder()).thenReturn(mock(HttpMessageEncoder.class));
    when(config.getNettyBootstrapOptions()).thenReturn(Maps.<String, Object>newHashMap());
    return config;
  }

  public static GnutellaServantModel initializeMe(Injector inj, int files, int size) {
    return initializeMe(inj, new InetSocketAddress(InetAddresses.forString("127.0.0.1"), 101),
        files, size);
  }

  public static GnutellaServantModel initializeMe(Injector inj, SocketAddress address, int files,
      int size) {
    NetworkIdentityManager manager = inj.getInstance(NetworkIdentityManager.class);
    NetworkIdentity me = manager.getMe();
    me.enterScope();
    GnutellaServantModel servant = inj.getInstance(GnutellaServantModel.class);
    me.exitScope();
    servant.setFileCount(files);
    servant.setFileSizeInKB(size);
    manager.setNetworkAddress(me, getGnutellaProtocol(inj), address);
    return servant;
  }

  public static Protocol getGnutellaProtocol(Injector inj) {
    return inj.getInstance(Key.get(Protocol.class, Gnutella.class));
  }

  public static ProtocolConfig getGnutellaProtocolConfig(Injector inj) {
    return inj.getInstance(Key.get(ProtocolConfig.class, Gnutella.class));
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
    JnutellaScopes.exitIdentityScope();
    JnutellaScopes.exitSessionScope();
  }
}
