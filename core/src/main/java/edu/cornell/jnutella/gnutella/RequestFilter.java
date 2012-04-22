package edu.cornell.jnutella.gnutella;

import edu.cornell.jnutella.gnutella.messages.GnutellaMessage;

public interface RequestFilter {
  boolean shouldAcceptPing(GnutellaMessage ping);
}
