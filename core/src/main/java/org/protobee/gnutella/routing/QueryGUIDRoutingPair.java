package org.protobee.gnutella.routing;

import org.protobee.identity.NetworkIdentity;

public class QueryGUIDRoutingPair {
    private final NetworkIdentity host;
    private final int routedResultCount;
    
    public QueryGUIDRoutingPair( NetworkIdentity host, int routedResultCount ) {
        this.host = host;
        this.routedResultCount = routedResultCount;
    }
    
    public NetworkIdentity getHost() {
        return host;
    }

    /**
     * @return Returns the routedResultCount.
     */
    public int getRoutedResultCount() {
        return routedResultCount;
    }
}

