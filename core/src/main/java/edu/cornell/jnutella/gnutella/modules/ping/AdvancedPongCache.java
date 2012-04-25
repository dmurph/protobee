package edu.cornell.jnutella.gnutella.modules.ping;

import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import edu.cornell.jnutella.gnutella.messages.GnutellaMessage;
import edu.cornell.jnutella.gnutella.messages.MessageHeader;
import edu.cornell.jnutella.gnutella.messages.PongBody;
import edu.cornell.jnutella.gnutella.modules.MaxTTL;
import edu.cornell.jnutella.guice.SessionScope;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.util.GUID;

@SessionScope
public class AdvancedPongCache {

  private final int maxPongsSent;
  private final int maxTtl;
  private final PingSessionModel pingModel;
  private final NetworkIdentity identity;
  private final AdvancedPongCacheGlobal global;

  @Inject
  public AdvancedPongCache(@MaxTTL int maxTtl, @MaxPongsSent int maxSent,
      PingSessionModel pingModel, NetworkIdentity identity, AdvancedPongCacheGlobal global) {
    this.pingModel = pingModel;
    this.identity = identity;
    this.maxPongsSent = maxSent;
    this.maxTtl = maxTtl;
    this.global = global;
  }

  public void addPong(MessageHeader header, PongBody body) {
    if (!GUID.isModernClient(header.getGuid())) {
      global.addToReservePongs(body);
    }
    synchronized (global.getCacheLock()) {
      global.getCache()[header.getHops()].add(new CacheEntry(body, identity));
    }
    
    // TODO demultiplex, need to change how session models are stored/registered
  }

  public void registerPing(MessageHeader header) {
    Preconditions.checkArgument(header.getTtl() != 2 || header.getHops() != 0,
        "Shouldn't be registering crawler pings");
    Preconditions.checkArgument(header.getPayloadType() == MessageHeader.F_PING);

    pingModel.setAcceptGuid(header.getGuid());
    for (int i = 1; i <= maxTtl; i++) {
      int needed = 0;
      if (i <= header.getTtl()) {
        needed = maxPongsSent / header.getTtl();
      }
      pingModel.getNeeded()[i].set(needed);
    }
  }

  public Iterable<GnutellaMessage> getCachedPongs(MessageHeader pingHeader) {
    Preconditions.checkArgument(pingHeader.getPayloadType() == MessageHeader.F_PING);

    List<GnutellaMessage> pongs = Lists.newArrayList();
    
    synchronized (global.getCacheLock()) {
      for (int ttl = 1; ttl <= pingHeader.getTtl(); ttl++) {
        List<CacheEntry> list = global.getCache()[ttl];
        int needed = pingModel.getNeeded()[ttl].get();

        for (int j = 0; needed > 0 && j < list.size(); j++) {
          CacheEntry entry = list.get(j);
          if (entry.identity == identity) {
            continue;
          }
          pongs.add(new GnutellaMessage(new MessageHeader(pingHeader.getGuid(),
              MessageHeader.F_PING_REPLY, (byte) 1, (byte) (ttl - 1)), entry.body));

          needed = pingModel.getNeeded()[ttl].decrementAndGet();
        }
      }
    }

    return pongs;
  }

  public boolean needsRebroadcasting() {
    return global.needsRebroadcasting();
  }

  public Set<PongBody> getReservePongs() {
    return global.getReserverPongs();
  }
  
  static class CacheEntry {
    final PongBody body;
    final NetworkIdentity identity;

    CacheEntry(PongBody body, NetworkIdentity identity) {
      this.body = body;
      this.identity = identity;
    }
  }

}
