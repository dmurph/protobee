package org.protobee.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.junit.Test;
import org.protobee.protocol.handlers.ChannelMessagePoster;
import org.protobee.protocol.handlers.ChannelMessagePoster.PosterEventFactory;

import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class PostHandlerTest {

  @Test
  public void testReceiver() throws Exception {

    EventBus testBus = new EventBus();
    ChannelMessagePoster poster =
        new ChannelMessagePoster(Sets.<PosterEventFactory<?>>newHashSet(
            new PosterEventFactory<String>(String.class) {
              @Override
              public Object createEvent(String message, ChannelHandlerContext context) {
                return message;
              }
            }, new PosterEventFactory<Integer>(Integer.class) {
              @Override
              public Object createEvent(Integer message, ChannelHandlerContext context) {
                return message;
              }
            }), testBus);

    final AtomicReference<String> strCalled = new AtomicReference<String>(null);
    final AtomicBoolean numCalled = new AtomicBoolean(false);
    Object substriber = new Object() {
      @SuppressWarnings("unused")
      @Subscribe
      public void str(String str) {
        strCalled.set(str);
      }

      @SuppressWarnings("unused")
      @Subscribe
      public void num(Integer num) {
        numCalled.set(true);
      }
    };

    testBus.register(substriber);

    poster.postEventForMessage(null, "hi");

    assertEquals("hi", strCalled.get());
    assertFalse(numCalled.get());
  }
}
