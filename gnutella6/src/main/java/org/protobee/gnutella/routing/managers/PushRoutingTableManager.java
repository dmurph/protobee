package org.protobee.gnutella.routing.managers;

import org.protobee.gnutella.routing.tables.GUIDRoutingTable;

import com.google.inject.Inject;
import com.google.inject.Singleton;


@Singleton
public class PushRoutingTableManager extends GUIDRoutingTableManager{

  // holds from 7-14 minutes of QueryReply GUIDs for push routes.
  private static long DEFAULT_LIFETIME = 7 * 60 * 1000;
  
  @Inject
  public PushRoutingTableManager(GUIDRoutingTable.Factory factory) {
    super(factory.create(DEFAULT_LIFETIME));
  }
  
}
