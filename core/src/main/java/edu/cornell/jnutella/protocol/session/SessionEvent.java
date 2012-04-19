package edu.cornell.jnutella.protocol.session;

import org.jboss.netty.channel.ChannelHandlerContext;

public abstract class SessionEvent {

  private final ChannelHandlerContext context;

  public SessionEvent(ChannelHandlerContext context) {
    super();
    this.context = context;
  }

  public ChannelHandlerContext getContext() {
    return context;
  }
}
