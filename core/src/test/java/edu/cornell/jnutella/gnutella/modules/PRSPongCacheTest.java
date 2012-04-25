package edu.cornell.jnutella.gnutella.modules;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.net.InetSocketAddress;

import org.junit.Test;

import com.google.common.collect.Iterables;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;

import edu.cornell.jnutella.AbstractTest;
import edu.cornell.jnutella.gnutella.messages.GnutellaMessage;
import edu.cornell.jnutella.gnutella.messages.MessageHeader;
import edu.cornell.jnutella.gnutella.messages.PongBody;
import edu.cornell.jnutella.gnutella.modules.ping.PRSPongCache;
import edu.cornell.jnutella.util.Clock;
import edu.cornell.jnutella.util.GUID;

public class PRSPongCacheTest extends AbstractTest {

  @Test
  public void testSingleEntry() {

    MessageHeader header = new MessageHeader(new byte[16], MessageHeader.F_PING_REPLY, (byte) 1, (byte) 1);
    PongBody body = new PongBody(new InetSocketAddress(80), 1, 1, null);
    GnutellaMessage message = new GnutellaMessage(header, body);

    PRSPongCache cache = injector.getInstance(PRSPongCache.class);
    cache.addPong(message);

    assertEquals(1, cache.size());
    assertEquals(body, Iterables.getOnlyElement(cache.getPongs(1)));
  }
  
  @Test
  public void testNoDuplicates() {
    MessageHeader header = new MessageHeader(new byte[16], MessageHeader.F_PING_REPLY, (byte) 1, (byte) 1);
    PongBody body = new PongBody(new InetSocketAddress(80), 1, 1, null);
    GnutellaMessage message = new GnutellaMessage(header, body);

    PRSPongCache cache = injector.getInstance(PRSPongCache.class);
    cache.addPong(message);
    cache.addPong(message);

    assertEquals(1, cache.size());
    assertEquals(body, Iterables.getOnlyElement(cache.getPongs(1)));
  }

  @Test
  public void testExpiredEntry() {
    final Clock clock = mock(Clock.class);
    when(clock.currentTimeMillis()).thenReturn(1000l, 2000l);

    Injector inj = getInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Clock.class).toInstance(clock);
      }
    });

    MessageHeader header =
        new MessageHeader(new byte[16], MessageHeader.F_PING_REPLY, (byte) 1, (byte) 1);
    PongBody body = new PongBody(new InetSocketAddress(80), 1, 1, null);
    GnutellaMessage message = new GnutellaMessage(header, body);

    PRSPongCache cache = inj.getInstance(PRSPongCache.class);
    cache.addPong(message);

    cache.filter(999);

    assertEquals(0, cache.size());
  }

  @Test
  public void testOneExpiredEntry() {
    final Clock clock = mock(Clock.class);
    when(clock.currentTimeMillis()).thenReturn(1000l, 2000l, 3000l);

    Injector inj = getInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Clock.class).toInstance(clock);
      }
    });

    MessageHeader header =
        new MessageHeader(new byte[16], MessageHeader.F_PING_REPLY, (byte) 1, (byte) 1);
    PongBody body = new PongBody(new InetSocketAddress(80), 1, 1, null);
    GnutellaMessage message = new GnutellaMessage(header, body);
    PongBody body2 = new PongBody(new InetSocketAddress(80), 1, 2, null);
    GnutellaMessage message2 = new GnutellaMessage(header, body2);

    PRSPongCache cache = inj.getInstance(PRSPongCache.class);
    cache.addPong(message);
    cache.addPong(message2);

    cache.filter(1500);

    assertEquals(1, cache.size());
    assertEquals(body2, Iterables.getOnlyElement(cache.getPongs(2)));
  }

  @Test
  public void testReturnLimitAndOrder() {

    int numToAdd = 20;
    int numToReturn = 10;

    PongBody[] bodies = new PongBody[numToAdd];

    PRSPongCache cache = injector.getInstance(PRSPongCache.class);

    for (int i = 0; i < numToAdd; i++) {
      bodies[i] = new PongBody(new InetSocketAddress(80), i + 1, 100, null);
    }

    for (PongBody pongBody : bodies) {
      MessageHeader header =
          new MessageHeader(new byte[16], MessageHeader.F_PING_REPLY, (byte) 1, (byte) 1);
      cache.addPong(new GnutellaMessage(header, pongBody));
    }

    Iterable<PongBody> cacheBodies = cache.getPongs(numToReturn);

    // should return in reverse order
    int bodyNum = 0;
    for (PongBody pongBody : cacheBodies) {
      assertEquals(bodies[bodyNum], pongBody);
      bodyNum++;
    }
    assertEquals(numToReturn, bodyNum);
  }
}
