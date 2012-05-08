package org.protobee.gnutella;

import org.protobee.gnutella.routing.QueryGUIDRoutingPair;

public interface RequestFilter {
  String shouldRouteQueryHitMessage(QueryGUIDRoutingPair qgrPair);
  String shouldRoutePushMessage(byte ttl, byte[] serventGUID);
}
