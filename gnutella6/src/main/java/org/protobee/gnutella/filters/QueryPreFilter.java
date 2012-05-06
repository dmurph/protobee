package org.protobee.gnutella.filters;

import org.protobee.gnutella.messages.GnutellaMessage;
import org.protobee.gnutella.messages.MessageHeader;
import org.protobee.gnutella.routing.managers.QueryRoutingTableManager;
import org.protobee.identity.IdentityTagManager;
import org.protobee.identity.NetworkIdentity;
import org.protobee.identity.NetworkIdentityManager;

import com.google.inject.Inject;
import com.google.inject.Singleton;


@Singleton
public class QueryPreFilter implements GnutellaPreFilter {

  private final QueryRoutingTableManager queryRTManager;
  private final NetworkIdentityManager identityManager;
  private final NetworkIdentity identity;
  
  @Inject
  public QueryPreFilter(QueryRoutingTableManager queryHitRTManager, NetworkIdentityManager identityManager, 
                        NetworkIdentity identity) {
    this.queryRTManager = queryHitRTManager;
    this.identityManager = identityManager;
    this.identity = identity;
  }

  @Override
  public String shouldFilter(GnutellaMessage message) {
    MessageHeader header = message.getHeader();
    
    // check query message type
    if (header.getPayloadType() != MessageHeader.F_QUERY) {
      return null;
    }
    
    if (queryRTManager.isRouted(header.getGuid())) {
      return "Query dropped - it's a duplicate.";
    }

    if (header.getHops() > 2 && identityManager.hasIdentity(identity)) {
        return "Query dropped - it's from a leaf and has hops greater than 2.";
    }

    return null;
  }
}
