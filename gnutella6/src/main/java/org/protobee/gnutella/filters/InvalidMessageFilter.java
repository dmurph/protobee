package org.protobee.gnutella.filters;

import org.protobee.gnutella.constants.MaxHops;
import org.protobee.gnutella.messages.GnutellaMessage;
import org.protobee.gnutella.messages.MessageHeader;
import org.protobee.util.PreFilter;

import com.google.inject.Inject;
import com.google.inject.Singleton;


@Singleton
public class InvalidMessageFilter implements PreFilter<GnutellaMessage> {

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
