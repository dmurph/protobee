package edu.cornell.jnutella;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import com.google.inject.Injector;

import edu.cornell.jnutella.guice.LogModule;
import edu.cornell.jnutella.identity.IdentityTagManager;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.identity.NetworkIdentityManager;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.protocol.ProtocolConfig;

public class IdentityTests {


  @Test
  public void testTagging() {

    Injector inj = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        install(new LogModule());
      }

      @SuppressWarnings("unused")
      @Provides
      @Singleton
      public Map<Protocol, ProtocolConfig> getConfigMap() {
        return ImmutableMap.of();
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
}
