package edu.cornell.jnutella.gnutella.modules.ping;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import edu.cornell.jnutella.gnutella.messages.PongBody;
import edu.cornell.jnutella.gnutella.modules.MaxTTL;
import edu.cornell.jnutella.gnutella.modules.ping.AdvancedPongCache.CacheEntry;
import edu.cornell.jnutella.util.Clock;

@Singleton
public class AdvancedPongCacheGlobal {

  private final Object cacheLock = new Object();
  private final List<CacheEntry>[] cache;
  private final Object reserveLock = new Object();
  private final Set<PongBody> reservePongs;
  private final int expireTime;
  private final Clock clock;
  private long nextExpire;

  @SuppressWarnings("unchecked")
  @Inject
  public AdvancedPongCacheGlobal(Clock clock, @MaxTTL int maxTtl, @PongExpireTime int expireTime,
      @MaxPongsSent int maxSent) {
    this.cache = (List<CacheEntry>[]) new List[maxTtl + 1];
    this.reservePongs = Sets.newHashSet();
    this.clock = clock;
    this.expireTime = expireTime;
    this.nextExpire = 0;
  }

  Object getCacheLock() {
    return cacheLock;
  }
  
  List<CacheEntry>[] getCache() {
    return cache;
  }
  
  boolean needsRebroadcasting() {
    synchronized (cacheLock) {
      long now = clock.currentTimeMillis();
      if (now > nextExpire) {
        moveToReserver();
        nextExpire = now + expireTime;
        return true;
      }
    }
    return false;
  }

  void addToReservePongs(PongBody body) {
    synchronized (reserveLock) {
      reservePongs.add(body);
    }
  }

  Set<PongBody> getReserverPongs() {
    synchronized (reserveLock) {
      return ImmutableSet.copyOf(reservePongs);
    }
  }

  private void moveToReserver() {
    for (List<CacheEntry> list : cache) {
      synchronized (reserveLock) {
        for (CacheEntry cacheEntry : list) {
          reservePongs.add(cacheEntry.body);
        }
      }
      list.clear();
    }
  }
}
