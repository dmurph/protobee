package org.protobee.events;

import org.jboss.netty.channel.ChannelHandlerContext;

public class BasicMessageReceivedEvent extends SessionEvent {

  private final Object message;

  public BasicMessageReceivedEvent(ChannelHandlerContext context, Object message) {
    super(context);
    this.message = message;
  }

  public Object getMessage() {
    return message;
  }
}
