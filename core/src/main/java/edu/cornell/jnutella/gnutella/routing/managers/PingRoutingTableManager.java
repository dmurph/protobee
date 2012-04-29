package edu.cornell.jnutella.gnutella.routing.managers;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import edu.cornell.jnutella.gnutella.routing.tables.GUIDRoutingTable;

@Singleton
public class PingRoutingTableManager extends GUIDRoutingTableManager{
  
  // holds from 2-4 minutes of ping GUIDs
  private static long DEFAULT_LIFETIME = 2 * 60 * 1000;
  
  @Inject
  public PingRoutingTableManager(GUIDRoutingTable.Factory factory) {
    super(factory.create(DEFAULT_LIFETIME));
  }
}
