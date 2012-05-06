package org.protobee.gnutella;

import org.protobee.gnutella.routing.QueryGUIDRoutingPair;
import org.protobee.gnutella.routing.managers.PushRoutingTableManager;

import com.google.inject.Singleton;


@Singleton
public class SimpleRequestFilter implements RequestFilter {

  private static final int MAX_ROUTED_QUERY_RESULTS = 200;

  private final PushRoutingTableManager pushRTManager;


  // @Inject
  public SimpleRequestFilter(){ 
    this.pushRTManager = null;
    };
  
  public SimpleRequestFilter(PushRoutingTableManager pushRTManager) {
    this.pushRTManager = pushRTManager;
  }

  @Override
  public String shouldRouteQueryHitMessage(QueryGUIDRoutingPair qgrPair, int ttl){
    if (qgrPair.getRoutedResultCount() >= MAX_ROUTED_QUERY_RESULTS){
      return "Query Hit not routed - routed result count to this node exceeds maximum.";
    }
    return "";
  }

  @Override
  public String shouldRoutePushMessage(byte ttl, byte[] serventGUID) {
    if (!pushRTManager.isRouted(serventGUID)){
      return "Push not routed - there is no routing address stored for this guid.";
    }
    return "";
  }

}
