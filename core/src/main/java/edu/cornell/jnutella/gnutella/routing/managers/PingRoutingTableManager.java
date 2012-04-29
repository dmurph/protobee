package edu.cornell.jnutella.gnutella.routing.managers;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import edu.cornell.jnutella.gnutella.routing.tables.GUIDRoutingTable;

@Singleton
public class PingRoutingTableManager extends GUIDRoutingTableManager{
  
  @Inject
  public PingRoutingTableManager(GUIDRoutingTable grtable) {
    super(grtable);
  }
  
}
