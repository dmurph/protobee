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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((host == null) ? 0 : host.hashCode());
    result = prime * result + routedResultCount;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    QueryGUIDRoutingPair other = (QueryGUIDRoutingPair) obj;
    if (host == null) {
      if (other.host != null) return false;
    } else if (!host.equals(other.host)) return false;
    if (routedResultCount != other.routedResultCount) return false;
    return true;
  }
}

