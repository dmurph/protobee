package org.protobee.examples.broadcast.filters;

import org.protobee.examples.broadcast.constants.IdSize;
import org.protobee.examples.broadcast.constants.MaxHops;
import org.protobee.examples.broadcast.constants.MaxTtl;
import org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage;
import org.protobee.examples.protos.Common.Header;
import org.protobee.util.PreFilter;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class InvalidMessageFilter implements PreFilter<BroadcastMessage> {

  private final int maxHops;
  private final int maxTtl;
  private final int idSize;

  @Inject
  public InvalidMessageFilter(@MaxHops int maxHops, @MaxTtl int maxTtl, @IdSize int idSize) {
    this.maxHops = maxHops;
    this.maxTtl = maxTtl;
    this.idSize = idSize;
  }

  @Override
  public String shouldFilter(BroadcastMessage message) {
    Header header = message.getHeader();
    int hops = header.getHops();
    int ttl = header.getTtl();
    if (ttl <= 0) {
      return "Header Ttl is " + ttl + ", which is <= 0";
    }
    if (header.getTtl() > maxTtl) {
      return "Ttl " + ttl + " is > than the max ttl of " + maxTtl;
    }
    if (hops < 0) {
      return "Illegal # of hops " + hops + ", must be >= 0";
    }
    if (hops > maxHops) {
      return "Illegal # of hops " + hops + ", must be < " + maxHops;
    }
    if (header.getId().size() != idSize) {
      return "Illegal id length " + header.getId().size() + ", must be of length " + idSize;
    }
    return null;
  }

}
