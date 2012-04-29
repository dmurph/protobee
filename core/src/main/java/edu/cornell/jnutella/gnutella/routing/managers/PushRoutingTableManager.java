package edu.cornell.jnutella.gnutella.routing.managers;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import edu.cornell.jnutella.gnutella.routing.tables.GUIDRoutingTable;

@Singleton
public class PushRoutingTableManager extends GUIDRoutingTableManager{

  @Inject
  public PushRoutingTableManager(GUIDRoutingTable grtable) {
    super(grtable);
  }

}
