package org.protobee.routing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.protobee.gnutella.AbstractGnutellaTest;
import org.protobee.gnutella.routing.QueryGUIDRoutingPair;
import org.protobee.gnutella.routing.managers.QueryRoutingTableManager;
import org.protobee.gnutella.util.GUID;
import org.protobee.identity.NetworkIdentity;

public class QueryRoutingTableTest extends AbstractGnutellaTest {

  @Test
  public void testAddAndGetRouting() {
    QueryRoutingTableManager manager = injector.getInstance(QueryRoutingTableManager.class);
    NetworkIdentity identity = mock(NetworkIdentity.class);
    QueryGUIDRoutingPair qgpair = new QueryGUIDRoutingPair(identity, 50);
    byte[] guid = new GUID().getBytes();
    manager.addRouting(guid, identity);
    
    assertTrue(manager.isRoutedForQuerys(guid));
    assertEquals((manager.findRoutingForQuerys(guid, 50)), qgpair);
    assertNull(manager.findRoutingForQuerys((new GUID()).getBytes(), 50));
    assertFalse(manager.isRoutedForQuerys((new GUID()).getBytes()));
    
    qgpair = new QueryGUIDRoutingPair(identity, 100);
    assertEquals((manager.findRoutingForQuerys(guid, 50)), qgpair);
  }
  
  @Test
  public void testSwitch() {
    QueryRoutingTableManager manager = injector.getInstance(QueryRoutingTableManager.class);
    NetworkIdentity identity = mock(NetworkIdentity.class);
    QueryGUIDRoutingPair qgpair = new QueryGUIDRoutingPair(identity, 50);
    byte[] guid = new GUID().getBytes();
    manager.addRouting(guid, identity);
    
    manager.doSwitch();
    manager.isRoutedForQuerys(guid);
    manager.doSwitch();
    
    assertTrue(manager.isRoutedForQuerys(guid));
    assertEquals((manager.findRoutingForQuerys(guid, 50)), qgpair);
    assertNull(manager.findRoutingForQuerys((new GUID()).getBytes(), 50));
    assertFalse(manager.isRoutedForQuerys((new GUID()).getBytes()));
  }
  
}
