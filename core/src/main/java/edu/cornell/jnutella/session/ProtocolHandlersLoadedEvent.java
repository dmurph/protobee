package edu.cornell.jnutella.session;

import org.jboss.netty.channel.ChannelHandlerContext;

public class ProtocolHandlersLoadedEvent extends SessionEvent {
  public ProtocolHandlersLoadedEvent(ChannelHandlerContext context) {
    super(context);
  }
}
