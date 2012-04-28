package edu.cornell.jnutella.gnutella.filters;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import edu.cornell.jnutella.gnutella.constants.MaxHops;
import edu.cornell.jnutella.gnutella.messages.GnutellaMessage;
import edu.cornell.jnutella.gnutella.messages.MessageHeader;

@Singleton
public class InvalidMessageFilter implements GnutellaPreFilter {

  private final int maxHops;

  @Inject
  public InvalidMessageFilter(@MaxHops int maxHops) {
    this.maxHops = maxHops;
  }

  @Override
  public String shouldFilter(GnutellaMessage message) {
    MessageHeader header = message.getHeader();
    int hops = header.getHops();
    int ttl = header.getTtl();
    if (hops < 0) {
      return "Hops cannot be < 0";
    }
    if (hops > maxHops) {
      return "Hops > max hops (" + maxHops + ")";
    }
    if (ttl <= 0) {
      return "Ttl cannot be <= 0";
    }
    return null;
  }
}
