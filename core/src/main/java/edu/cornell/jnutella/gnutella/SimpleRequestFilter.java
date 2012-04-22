package edu.cornell.jnutella.gnutella;

import com.google.inject.Singleton;

import edu.cornell.jnutella.gnutella.messages.GnutellaMessage;
import edu.cornell.jnutella.gnutella.messages.MessageHeader;

@Singleton
public class SimpleRequestFilter implements RequestFilter {

  @Override
  public boolean shouldAcceptPing(GnutellaMessage ping) {
    MessageHeader header = ping.getHeader();
    byte ttl = header.getTtl();
    byte hops = header.getHops();

    if ((ttl + hops > 2)/* && !hostMgr.areIncommingSlotsAdvertised() */) {
      // TODO return false
      return true;
    }
    return true;
  }

}
