package org.protobee.gnutella.filters;

import java.util.Set;

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
  private final IdentityTagManager tagManager;
  private final NetworkIdentity identity;
  
  @Inject
  public QueryPreFilter(QueryRoutingTableManager queryHitRTManager, NetworkIdentityManager identityManager, 
                        IdentityTagManager tagManager, NetworkIdentity identity) {
    this.queryRTManager = queryHitRTManager;
    this.identityManager = identityManager;
    this.tagManager = tagManager;
    this.identity = identity;
  }

  @Override
  public String shouldFilter(GnutellaMessage message) {
    MessageHeader header = message.getHeader();
    
    // check query message type
    if (header.getPayloadType() != MessageHeader.F_QUERY) {
      return "";
    }
    
    // Drop already seen query
    if (queryRTManager.isRouted(header.getGuid())) {
      return "Query dropped - it's a duplicate.";
    }

    // Drop query from leaf with hops > 2
    if (header.getHops() > 2) {
      Set<NetworkIdentity> leafs = identityManager.getTaggedIdentities(tagManager.getLeafKey());
      if (leafs.contains(identity)){
        return "Query dropped - it's from a leaf and has hops greater than 2.";
      }
    }

    return "";
  }
}
