package org.protobee.gnutella.filters;

import org.protobee.gnutella.messages.GnutellaMessage;
import org.protobee.gnutella.messages.MessageHeader;
import org.protobee.gnutella.routing.managers.QueryRoutingTableManager;
import org.protobee.identity.IdentityTagManager;
import org.protobee.identity.NetworkIdentity;
import org.protobee.util.PreFilter;

import com.google.inject.Inject;
import com.google.inject.Singleton;


@Singleton
public class QueryPreFilter implements PreFilter<GnutellaMessage> {

  private final QueryRoutingTableManager queryRTManager;
  private final NetworkIdentity identity;
  private final IdentityTagManager tagManager;
  
  @Inject
  public QueryPreFilter(QueryRoutingTableManager queryHitRTManager, 
                        NetworkIdentity identity, IdentityTagManager tagManager) {
    this.queryRTManager = queryHitRTManager;
    this.identity = identity;
    this.tagManager = tagManager;
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

    if (header.getHops() > 2 && identity.hasTag(tagManager.getLeafKey())) {
        return "Query dropped - it's from a leaf and has hops greater than 2.";
    }

    return null;
  }
}
