package org.protobee.gnutella.filters;

import java.util.Arrays;

import org.protobee.gnutella.GnutellaServantModel;
import org.protobee.gnutella.messages.GnutellaMessage;
import org.protobee.gnutella.messages.MessageHeader;
import org.protobee.gnutella.messages.QueryHitBody;
import org.protobee.gnutella.routing.IdentityHash;
import org.protobee.gnutella.routing.InvalidMessageException;
import org.protobee.gnutella.routing.managers.QueryRoutingTableManager;
import org.protobee.gnutella.util.GUID;
import org.protobee.util.PreFilter;

import com.google.inject.Inject;
import com.google.inject.Singleton;


@Singleton
public class QueryHitPreFilter implements PreFilter<GnutellaMessage> {

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
      return null;
    }
    
    // generate body
    QueryHitBody queryHitBody = (QueryHitBody) message.getBody();
    
    // body check
    if(queryHitBody.getServantID() == null){ 
      return "Query Hit dropped - servent id cant be null.";
    }
    if (Arrays.equals(servantModel.getGuid(), queryHitBody.getServantID())){
      return "Query Hit dropped - my query response should never reach me.";
    }
    
    //generate queryHash
    GUID messageGuid;
    try {
      messageGuid = new GUID(header.getGuid());
    } catch (InvalidMessageException e) {
      return "Query Hit dropped - query guid could not be rendered from guid bytes.";
    }
    
    if (queryHitRTManager.hasRoutingForQuerys(messageGuid, queryHitBody.getNumHits())){
      return "Query Hit dropped - no query guid routing pair was found.";
    }
    
    IdentityHash queryHash = new IdentityHash(header.getGuid(), queryHitBody.getUrns()); 

    // check queryHash
    if(Arrays.equals(queryHitBody.getServantID(), queryHash.getGuid())){
      return "Query Hit dropped - message id can't equal servent id.";
    }
    if (queryHitRTManager.hasQueryHit(queryHash)){
      return "Query Hit dropped - it's a duplicate.";
    }
    return null;
  }
}
