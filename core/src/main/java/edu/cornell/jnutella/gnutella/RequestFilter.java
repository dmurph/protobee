package edu.cornell.jnutella.gnutella;

import edu.cornell.jnutella.gnutella.messages.GnutellaMessage;
import edu.cornell.jnutella.gnutella.messages.QueryBody;
import edu.cornell.jnutella.gnutella.routing.IdentityHash;
import edu.cornell.jnutella.gnutella.routing.QueryGUIDRoutingPair;
import edu.cornell.jnutella.util.GUID;

public interface RequestFilter {
  boolean shouldAcceptPing(GnutellaMessage ping);
  boolean shouldAcceptRoutingMessage(GnutellaMessage routingMessage);
  boolean shouldAcceptQueryHitMessage(QueryGUIDRoutingPair qgrPair, GUID localGUID, GUID responderGUID, IdentityHash queryHash);
  boolean shouldRouteQueryHitMessage(QueryGUIDRoutingPair qgrPair, int ttl);
  boolean shouldAcceptQueryMessage(GUID messageGUID, byte b, QueryBody body);
  boolean shouldRoutePushMessage(byte ttl, GUID serventGUID);
}
