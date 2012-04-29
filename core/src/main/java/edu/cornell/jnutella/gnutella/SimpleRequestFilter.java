package edu.cornell.jnutella.gnutella;

import java.util.Arrays;
import java.util.Set;

import com.google.inject.Singleton;

import edu.cornell.jnutella.gnutella.messages.GnutellaMessage;
import edu.cornell.jnutella.gnutella.messages.MessageHeader;
import edu.cornell.jnutella.gnutella.messages.QueryBody;
import edu.cornell.jnutella.gnutella.routing.IdentityHash;
import edu.cornell.jnutella.gnutella.routing.QueryGUIDRoutingPair;
import edu.cornell.jnutella.gnutella.routing.managers.CoreRoutingTableManager;
import edu.cornell.jnutella.gnutella.routing.managers.PingRoutingTableManager;
import edu.cornell.jnutella.gnutella.routing.managers.PushRoutingTableManager;
import edu.cornell.jnutella.gnutella.routing.managers.QueryRoutingTableManager;
import edu.cornell.jnutella.identity.IdentityTagManager;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.identity.NetworkIdentityManager;

@Singleton
public class SimpleRequestFilter implements RequestFilter {

  // TODO move parts to prefilters 
  
  private static final int MAX_ROUTED_QUERY_RESULTS = 200;

  private final PingRoutingTableManager pingRTManager;
  private final PushRoutingTableManager pushRTManager;
  private final QueryRoutingTableManager queryRTManager;
  private final CoreRoutingTableManager coreRTManager;
  private final NetworkIdentityManager identityManager;
  private final IdentityTagManager tagManager;
  private final NetworkIdentity identity;


  // @Inject
  public SimpleRequestFilter(){ 
    this.pingRTManager = null;
    this.pushRTManager = null;
    this.queryRTManager = null;
    this.coreRTManager = null;
    this.identityManager = null;
    this.tagManager = null;
    this.identity = null;
  };
  
  public SimpleRequestFilter(PingRoutingTableManager pingRTManager, PushRoutingTableManager pushRTManager, 
                             QueryRoutingTableManager queryHitRTManager, CoreRoutingTableManager queryRTManager,
                             NetworkIdentityManager identityManager, IdentityTagManager tagManager,
                             NetworkIdentity identity) {
    this.pingRTManager = pingRTManager;
    this.pushRTManager = pushRTManager;
    this.queryRTManager = queryHitRTManager;
    this.coreRTManager = queryRTManager;
    this.identityManager = identityManager;
    this.tagManager = tagManager;
    this.identity = identity;
  }

  @Override
  public boolean shouldAcceptPing(GnutellaMessage ping) {
    MessageHeader header = ping.getHeader();

    if ((header.getTtl() + header.getHops() > 2)/* && !hostMgr.areIncommingSlotsAdvertised() */) {
      return true;
    }
    return true;
  }

  @Override
  public boolean shouldAcceptRoutingMessage(GnutellaMessage routingMessage) {
    // TODO Auto-generated method stub IF ULTRAPEER I THINK
    return false;
  }

  @Override
  public boolean shouldAcceptQueryMessage(byte[] messageGUID, byte hops, QueryBody body) {
    
    // Drop already seen query
    if (queryRTManager.isRouted(messageGUID)) {
      return false;
    }

    // Drop query from leaf with hops > 2
    if (hops > 2) {
      Set<NetworkIdentity> leafs = identityManager.getTaggedIdentities(tagManager.getLeafKey());
      if (leafs.contains(identity)){
          return false;
      }
    }

    return true;
  }

  @Override 
  public boolean shouldAcceptQueryHitMessage(QueryGUIDRoutingPair qgrPair, byte[] localGUID, byte[] responderGUID, IdentityHash queryHash) {
    if (qgrPair == null){
      return false;
    }
    if (Arrays.equals(localGUID, responderGUID)){ // My query response should never reach me
      return false;
    }
    if(Arrays.equals(responderGUID, queryHash.getGuid())){ // Message id can't equal servent id
      return false;
    }
    if(responderGUID == null){ // servent id cant be null
      return false;
    }
    return (!queryRTManager.hasQueryHit(queryHash));
  }

  @Override
  public boolean shouldRouteQueryHitMessage(QueryGUIDRoutingPair qgrPair, int ttl){
    if ( ttl <= 0 ){
      return false;
    }
    return (qgrPair.getRoutedResultCount() < MAX_ROUTED_QUERY_RESULTS);
  }

  @Override
  public boolean shouldRoutePushMessage(byte ttl, byte[] serventGUID) {
    if (ttl <= 0){
      return false;
    }
    if (!pushRTManager.isRouted(serventGUID)){
      return false;
    }
    return true;
  }

}
