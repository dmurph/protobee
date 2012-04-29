package org.protobee;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.protobee.guice.IdentityScope;
import org.protobee.guice.IdentityScopeMap;
import org.protobee.guice.JnutellaScopes;
import org.protobee.guice.LogModule;
import org.protobee.identity.IdentityTagManager;
import org.protobee.identity.NetworkIdentity;
import org.protobee.identity.NetworkIdentityManager;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.ProtocolConfig;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;


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
      @Singleton
      public Set<ProtocolConfig> getProtocols() {
        return ImmutableSet.of();
      }

      @SuppressWarnings("unused")
      @Provides
      @Singleton
      public Set<Protocol> getProtocolss() {
        return ImmutableSet.of();
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
    Protocol protocol = config.get();

    Injector inj = getInjectorWithProtocolConfig(config);

    NetworkIdentityManager manager = inj.getInstance(NetworkIdentityManager.class);
    SocketAddress address = new InetSocketAddress(80);
    NetworkIdentity identity = manager.getNetworkIdentityWithNewConnection(protocol, address);

    assertEquals(address, identity.getSendingAddress(protocol));
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
    Protocol protocol = config.get();

    Injector inj = getInjectorWithProtocolConfig(config);

    NetworkIdentityManager manager = inj.getInstance(NetworkIdentityManager.class);
    SocketAddress address = new InetSocketAddress(80);
    NetworkIdentity identity = manager.getNetworkIdentityWithNewConnection(protocol, address);
    createSession(identity, inj, config);

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
