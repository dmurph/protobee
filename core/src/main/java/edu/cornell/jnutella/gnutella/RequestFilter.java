package edu.cornell.jnutella.gnutella;

import edu.cornell.jnutella.gnutella.messages.GnutellaMessage;
import edu.cornell.jnutella.gnutella.messages.QueryBody;
import edu.cornell.jnutella.gnutella.routing.IdentityHash;
import edu.cornell.jnutella.gnutella.routing.QueryGUIDRoutingPair;

public interface RequestFilter {
  boolean shouldAcceptPing(GnutellaMessage ping);
  boolean shouldAcceptRoutingMessage(GnutellaMessage routingMessage);
  boolean shouldAcceptQueryHitMessage(QueryGUIDRoutingPair qgrPair, byte[] localGUID, byte[] responderGUID, IdentityHash queryHash);
  boolean shouldRouteQueryHitMessage(QueryGUIDRoutingPair qgrPair, int ttl);
  boolean shouldAcceptQueryMessage(byte[] messageGUID, byte b, QueryBody body);
  boolean shouldRoutePushMessage(byte ttl, byte[] serventGUID);
}
