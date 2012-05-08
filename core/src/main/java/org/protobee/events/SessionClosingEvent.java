package org.protobee.events;

import org.jboss.netty.channel.ChannelHandlerContext;

public class SessionClosingEvent extends SessionEvent {
  public SessionClosingEvent(ChannelHandlerContext context) {
    super(context);
  }
}
