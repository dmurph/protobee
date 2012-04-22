package edu.cornell.jnutella.gnutella;

import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.session.SessionModel;

public class GnutellaMessageHelper {

  private final Protocol gnutella;

  public GnutellaMessageHelper(@Gnutella Protocol protocol) {
    gnutella = protocol;
  }

  public void sendMessage(NetworkIdentity identity) {
    if (identity.hasCurrentSession(gnutella)) {
      SessionModel session = identity.getCurrentSession(gnutella);
      session.enterScope();
    }
  }


}
