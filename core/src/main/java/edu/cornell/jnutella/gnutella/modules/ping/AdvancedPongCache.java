package edu.cornell.jnutella.gnutella.modules.ping;

import java.util.List;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import edu.cornell.jnutella.gnutella.constants.MaxTTL;
import edu.cornell.jnutella.gnutella.messages.PongBody;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.util.Clock;

@Singleton
public class AdvancedPongCache {

  private final Object cacheLock = new Object();
  private final List<CacheEntry>[] cache;
  private final Object reserveLock = new Object();
  private final Set<PongBody> reservePongs;
  private final int expireTime;
  private final Clock clock;
  private long nextExpire;

  @SuppressWarnings("unchecked")
  @Inject
  public AdvancedPongCache(Clock clock, @MaxTTL int maxTtl, @PongExpireTime int expireTime) {
    this.cache = (List<CacheEntry>[]) new List[maxTtl + 1];
    for (int i = 0; i < cache.length; i++) {
      cache[i] = Lists.newArrayList();
    }
    this.reservePongs = Sets.newHashSet();
    this.clock = clock;
    this.expireTime = expireTime;
    this.nextExpire = expireTime;
  }

  public Object getCacheLock() {
    return cacheLock;
  }

  public List<CacheEntry>[] getCache() {
    return cache;
  }

  public boolean needsRebroadcasting() {
    synchronized (cacheLock) {
      long now = clock.currentTimeMillis();
      if (now > nextExpire) {
        moveToReserve();
        nextExpire = now + expireTime;
        return true;
      }
    }
    return false;
  }

  public void addToReservePongs(PongBody body) {
    synchronized (reserveLock) {
      reservePongs.add(body);
    }
  }

  public Set<PongBody> getReserverPongs() {
    synchronized (reserveLock) {
      return ImmutableSet.copyOf(reservePongs);
    }
  }

  private void moveToReserve() {
    for (List<CacheEntry> list : cache) {
      synchronized (reserveLock) {
        for (CacheEntry cacheEntry : list) {
          reservePongs.add(cacheEntry.body);
        }
      }
      list.clear();
    }
  }

  public static class CacheEntry {
    final PongBody body;
    final NetworkIdentity identity;

    public CacheEntry(PongBody body, NetworkIdentity identity) {
      this.body = body;
      this.identity = identity;
    }
    
    @VisibleForTesting
    public PongBody getBody() {
      return body;
    }
    
    @VisibleForTesting
    public NetworkIdentity getIdentity() {
      return identity;
    }
  }
}
