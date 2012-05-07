package org.protobee.routing;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.protobee.gnutella.AbstractGnutellaTest;
import org.protobee.gnutella.routing.managers.PushRoutingTableManager;
import org.protobee.gnutella.util.GUID;
import org.protobee.identity.NetworkIdentity;

public class GUIDRoutingTableTest extends AbstractGnutellaTest {

  @Test
  public void testAddAndGetRouting() {
    PushRoutingTableManager manager = injector.getInstance(PushRoutingTableManager.class);
    NetworkIdentity identity = mock(NetworkIdentity.class);
    byte[] guid = new GUID().getBytes();
    manager.addRouting(guid, identity);
    
    assertTrue(manager.isRouted(guid));
    assertEquals(manager.findRouting(guid), identity);
    assertNull(manager.findRouting((new GUID()).getBytes()));
    assertFalse(manager.isRouted((new GUID()).getBytes()));
  }
  
  @Test
  public void testSwitch() {
    PushRoutingTableManager manager = injector.getInstance(PushRoutingTableManager.class);
    NetworkIdentity identity = mock(NetworkIdentity.class);
    byte[] guid = new GUID().getBytes();
    manager.addRouting(guid, identity);
    
    manager.doSwitch();
    manager.isRouted(guid);
    manager.doSwitch();
    
    assertTrue(manager.isRouted(guid));
    assertEquals(manager.findRouting(guid), identity);
    assertNull(manager.findRouting((new GUID()).getBytes()));
    assertFalse(manager.isRouted((new GUID()).getBytes()));
  }
  
}
