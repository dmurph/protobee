package org.protobee.protocol.handlers;

import java.util.Map;
import java.util.Set;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelDownstreamHandler;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.protobee.events.MessageReceivedEvent;

import com.google.common.eventbus.EventBus;

public class ProtocolMessageReceiver extends SimpleChannelUpstreamHandler {

  private final Set<Class<?>> allowedClasses;
  private final FilterMode mode;
  private final EventBus bus;

  public ProtocolMessageReceiver(Set<Class<?>> allowedClasses, FilterMode mode, EventBus bus) {
    this.allowedClasses = allowedClasses;
    this.mode = mode;
    this.bus = bus;
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

    if (allowedClasses.contains(e.getMessage().getClass())) {
      bus.post(getEvent(ctx, e.getMessage(), e.getMessage().getClass()));
    }

    // TODO Auto-generated method stub
    super.messageReceived(ctx, e);
  }

  private <T> MessageReceivedEvent<T> getEvent(ChannelHandlerContext ctx, Object object,
      Class<T> objectClass) {
    return new MessageReceivedEvent<T>(ctx, objectClass.cast(object), objectClass);
  }
}
