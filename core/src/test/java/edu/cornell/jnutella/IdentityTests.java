package edu.cornell.jnutella;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import edu.cornell.jnutella.guice.IdentityScope;
import edu.cornell.jnutella.guice.IdentityScopeMap;
import edu.cornell.jnutella.guice.JnutellaScopes;
import edu.cornell.jnutella.guice.LogModule;
import edu.cornell.jnutella.identity.IdentityTagManager;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.identity.NetworkIdentityManager;
import edu.cornell.jnutella.identity.ProtocolIdentityModel;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.protocol.ProtocolConfig;

public class IdentityTests extends AbstractTest {


  @Test
  public void testTagging() {

    Injector inj = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        install(new LogModule());

        bindScope(IdentityScope.class, JnutellaScopes.IDENTITY);
      }

      @SuppressWarnings("unused")
      @Provides
      @Singleton
      public Map<Protocol, ProtocolConfig> getConfigMap() {
        return ImmutableMap.of();
      }

      @SuppressWarnings("unused")
      @Provides
      @IdentityScopeMap
      public Map<String, Object> createScopeMap() {
        return Maps.newHashMap();
      }
    });

    NetworkIdentityManager manager = inj.getInstance(NetworkIdentityManager.class);
    NetworkIdentity a = manager.createNetworkIdentity();
    NetworkIdentity b = manager.createNetworkIdentity();
    NetworkIdentity c = manager.createNetworkIdentity();

    IdentityTagManager tags = inj.getInstance(IdentityTagManager.class);

    Object tag1 = tags.generateKey(10);
    Object tag2 = tags.generateKey(11);

    manager.tagIdentity(tag1, a);
    manager.tagIdentity(tag2, a);
    manager.tagIdentity(tag1, b);

    assertEquals(2, a.getTags().size());
    assertEquals(1, b.getTags().size());
    assertEquals(0, c.getTags().size());

    Set<NetworkIdentity> tag1s = manager.getTaggedIdentities(tag1);
    Set<NetworkIdentity> tag2s = manager.getTaggedIdentities(tag2);

    assertEquals(2, tag1s.size());
    assertTrue(tag1s.contains(a));
    assertTrue(tag1s.contains(b));

    assertEquals(1, tag2s.size());
    assertTrue(tag1s.contains(a));
  }

  @Test
  public void testIdentityInScope() {
    NetworkIdentity identity = createIdentity(injector);
    NetworkIdentity identity2 = createIdentity(injector);
    identity.enterScope();
    assertSame(identity, injector.getInstance(NetworkIdentity.class));
    identity.exitScope();
    identity2.enterScope();
    assertSame(identity2, injector.getInstance(NetworkIdentity.class));
    identity2.exitScope();
  }

  @Test
  public void testExceptionOnTwoIdentityScopes() {
    NetworkIdentity identity = createIdentity(injector);
    identity.enterScope();
    boolean caught = false;
    try {
      identity.enterScope();
    } catch (Exception e) {
      caught = true;
    }
    assertTrue(caught);
    identity.exitScope();
  }

  @Test
  public void testAddressSet() {
    ProtocolConfig config = mockDefaultProtocolConfig();
    ProtocolIdentityModel mockedModel = config.createIdentityModel();
    Protocol protocol = config.get();

    Injector inj = getInjectorWithProtocolConfig(config);

    NetworkIdentityManager manager = inj.getInstance(NetworkIdentityManager.class);
    SocketAddress address = new InetSocketAddress(80);
    NetworkIdentity identity = manager.getNetworkIdentityWithNewConnection(protocol, address);

    assertEquals(mockedModel, identity.getModel(protocol));
    verify(mockedModel).setNetworkAddress(address);
  }

  public void testErrorOnSameAddress() {
    ProtocolConfig config = mockDefaultProtocolConfig();
    Protocol protocol = config.get();

    ProtocolConfig config2 = mockDefaultProtocolConfig();
    Protocol protocol2 = config.get();

    Injector inj = getInjectorWithProtocolConfig(config, config2);

    NetworkIdentityManager manager = inj.getInstance(NetworkIdentityManager.class);
    SocketAddress address = new InetSocketAddress(80);

    manager.getNetworkIdentityWithNewConnection(protocol, address);

    boolean caught = false;
    try {
      manager.getNetworkIdentityWithNewConnection(protocol2, address);
    } catch (IllegalStateException e) {
      caught = true;
    }
    assertTrue(caught);
  }

  @Test
  public void testErrorOnCurrentSession() {
    ProtocolConfig config = mockDefaultProtocolConfig();
    ProtocolIdentityModel mockedModel = config.createIdentityModel();
    when(mockedModel.hasCurrentSession()).thenReturn(true);
    Protocol protocol = config.get();

    Injector inj = getInjectorWithProtocolConfig(config);

    NetworkIdentityManager manager = inj.getInstance(NetworkIdentityManager.class);
    SocketAddress address = new InetSocketAddress(80);
    manager.getNetworkIdentityWithNewConnection(protocol, address);


    SocketAddress address2 = new InetSocketAddress(80);
    boolean caught = false;
    try {
      manager.getNetworkIdentityWithNewConnection(protocol, address2);
    } catch (IllegalStateException e) {
      // this could also be catching the fact that we're on the same address...
      caught = true;
    }
    assertTrue(caught);
  }
}
