package org.protobee.gnutella;

import org.protobee.gnutella.messages.GnutellaMessage;
import org.protobee.gnutella.messages.QueryBody;
import org.protobee.gnutella.routing.IdentityHash;
import org.protobee.gnutella.routing.QueryGUIDRoutingPair;

public interface RequestFilter {
  boolean shouldAcceptPing(GnutellaMessage ping);
  boolean shouldAcceptRoutingMessage(GnutellaMessage routingMessage);
  boolean shouldAcceptQueryHitMessage(QueryGUIDRoutingPair qgrPair, byte[] localGUID, byte[] responderGUID, IdentityHash queryHash);
  boolean shouldRouteQueryHitMessage(QueryGUIDRoutingPair qgrPair, int ttl);
  boolean shouldAcceptQueryMessage(byte[] messageGUID, byte b, QueryBody body);
  boolean shouldRoutePushMessage(byte ttl, byte[] serventGUID);
}
