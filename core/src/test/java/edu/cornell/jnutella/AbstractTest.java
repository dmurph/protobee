package edu.cornell.jnutella;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.jboss.netty.handler.codec.http.HttpMessageDecoder;
import org.jboss.netty.handler.codec.http.HttpMessageEncoder;
import org.junit.After;
import org.junit.Before;

import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

import edu.cornell.jnutella.guice.JnutellaMainModule;
import edu.cornell.jnutella.guice.JnutellaScopes;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.identity.NetworkIdentityFactory;
import edu.cornell.jnutella.identity.ProtocolIdentityModel;
import edu.cornell.jnutella.modules.ProtocolModule;
import edu.cornell.jnutella.plugin.PluginGuiceModule;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.protocol.ProtocolConfig;
import edu.cornell.jnutella.session.ProtocolSessionModel;

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
        addProtocolConfig(configs);
      }
    };
  }

  public static ProtocolConfig mockDefaultProtocolConfig() {
    ProtocolConfig config = mock(ProtocolConfig.class);
    when(config.get()).thenReturn(mock(Protocol.class));
    when(config.createIdentityModel()).thenReturn(mock(ProtocolIdentityModel.class));
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
    when(config.createIdentityModel()).thenReturn(mock(ProtocolIdentityModel.class));
    when(config.createSessionModel()).thenReturn(sessionModel);
    when(config.createRequestDecoder()).thenReturn(mock(HttpMessageDecoder.class));
    when(config.createRequestEncoder()).thenReturn(mock(HttpMessageEncoder.class));
    when(config.getNettyBootstrapOptions()).thenReturn(Maps.<String, Object>newHashMap());
    return config;
  }

  @After
  public void freeScopes() {
    JnutellaScopes.exitIdentityScope();
    JnutellaScopes.exitSessionScope();
  }
}
