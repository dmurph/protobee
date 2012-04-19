package edu.cornell.jnutella.protocol.session;

import org.jboss.netty.channel.ChannelHandlerContext;

public class ProtocolHandlersLoadedEvent extends SessionEvent {
  public ProtocolHandlersLoadedEvent(ChannelHandlerContext context) {
    super(context);
  }
}
