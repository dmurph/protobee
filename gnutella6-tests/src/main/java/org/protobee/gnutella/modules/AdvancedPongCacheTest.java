package org.protobee.gnutella.modules;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.protobee.gnutella.messages.PongBody;
import org.protobee.gnutella.modules.ping.AdvancedPongCache;
import org.protobee.gnutella.modules.ping.AdvancedPongCache.CacheEntry;
import org.protobee.util.Clock;


public class AdvancedPongCacheTest {

  @Test
  public void testExpire() {
    Clock mockClock = mock(Clock.class);
    when(mockClock.currentTimeMillis()).thenReturn(1000l, 2000l);

    AdvancedPongCache cache = new AdvancedPongCache(mockClock, 6, 1001);

    PongBody body = new PongBody(null, 1, 1, null);
    cache.getCache()[0].add(new CacheEntry(body, null));

    assertFalse(cache.needsRebroadcasting());
    assertEquals(0, cache.getReserverPongs().size());

    assertTrue(cache.needsRebroadcasting());
    assertEquals(1, cache.getReserverPongs().size());
    assertEquals(body, cache.getReserverPongs().iterator().next());
  }

}
