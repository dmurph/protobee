package org.protobee.protocol.handlers;

import java.util.Map;
import java.util.Set;

import org.jboss.netty.channel.ChannelHandlerContext;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.EventBus;

public class ChannelMessagePoster {

  private final Map<Class<?>, PosterEventFactory<?>> events;
  private final EventBus bus;

  public ChannelMessagePoster(Set<PosterEventFactory<?>> events, EventBus bus) {
    Preconditions.checkNotNull(events);
    Preconditions.checkNotNull(bus);
    this.bus = bus;

    ImmutableMap.Builder<Class<?>, PosterEventFactory<?>> builder = ImmutableMap.builder();
    for (PosterEventFactory<?> eventFactory : events) {
      builder.put(eventFactory.getMessageClass(), eventFactory);
    }
    this.events = builder.build();
  }

  /**
   * Posts the message to the event bus
   * 
   * @return if an event factory for the message was found
   */
  public boolean postEventForMessage(ChannelHandlerContext context, Object message) {
    if (events.containsKey(message.getClass())) {
      bus.post(createEvent(events.get(message.getClass()), message, context));
      return true;
    }
    return false;
  }

  private <T> Object createEvent(PosterEventFactory<T> factory, Object object,
      ChannelHandlerContext ctx) {
    Class<T> klass = factory.getMessageClass();
    return factory.createEvent(klass.cast(object), ctx);
  }

  /**
   * This class is how the {@link UpstreamMessagePosterHandler} creates an event for a given
   * message. Each factory given to a {@link UpstreamMessagePosterHandler} should probably give a
   * distinct event type, as per the functionality of {@link EventBus}
   * 
   * @author Daniel
   * @param <T>
   */
  public static abstract class PosterEventFactory<T> {
    private final Class<T> klass;

    public PosterEventFactory(Class<T> messageClass) {
      this.klass = messageClass;
    }

    public Class<T> getMessageClass() {
      return klass;
    }

    public abstract Object createEvent(T message, ChannelHandlerContext context);
  }
}
