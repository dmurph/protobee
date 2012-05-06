package org.protobee.gnutella.filters;

import java.util.Arrays;

import org.protobee.gnutella.GnutellaServantModel;
import org.protobee.gnutella.messages.GnutellaMessage;
import org.protobee.gnutella.messages.MessageHeader;
import org.protobee.gnutella.messages.QueryHitBody;
import org.protobee.gnutella.routing.IdentityHash;
import org.protobee.gnutella.routing.InvalidMessageException;
import org.protobee.gnutella.routing.QueryGUIDRoutingPair;
import org.protobee.gnutella.routing.managers.QueryRoutingTableManager;
import org.protobee.gnutella.util.GUID;

import com.google.inject.Inject;
import com.google.inject.Singleton;


@Singleton
public class QueryHitPreFilter implements GnutellaPreFilter {

  private final GnutellaServantModel servantModel;
  private final QueryRoutingTableManager queryHitRTManager;
  
  @Inject
  public QueryHitPreFilter(GnutellaServantModel servantModel, 
                           QueryRoutingTableManager queryHitRTManager) {
    this.servantModel = servantModel;
    this.queryHitRTManager = queryHitRTManager;
  }

  @Override
  public String shouldFilter(GnutellaMessage message) {
    MessageHeader header = message.getHeader();
    
    // check query hit message type
    if (header.getPayloadType() != MessageHeader.F_QUERY_REPLY) {
      return "";
    }
    
    // generate info
    QueryHitBody queryHitBody = (QueryHitBody) message.getBody();
    QueryGUIDRoutingPair qgrPair;
    try {
      qgrPair = queryHitRTManager.findRoutingForQuerys(new GUID(header.getGuid()),
          queryHitBody.getNumHits());
    } catch (InvalidMessageException e) {
      return "Query Hit dropped - query guid could not be rendered from guid bytes.";
    }
    
    IdentityHash queryHash = new IdentityHash(header.getGuid(), queryHitBody.getUrns());
    

    // other checks
    if (qgrPair == null){
      return "Query Hit dropped - no query guid routing pair was found.";
    }
    if (Arrays.equals(servantModel.getGuid(), queryHitBody.getServantID())){
      return "Query Hit dropped - my query response should never reach me.";
    }
    if(Arrays.equals(queryHitBody.getServantID(), queryHash.getGuid())){
      return "Query Hit dropped - message id can't equal servent id.";
    }
    if(queryHitBody.getServantID() == null){ 
      return "Query Hit dropped - servent id cant be null.";
    }
    if (queryHitRTManager.hasQueryHit(queryHash)){
      return "Query Hit dropped - it's a duplicate.";
    }
    return "";
  }
}
