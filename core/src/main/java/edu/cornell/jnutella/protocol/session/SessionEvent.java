package edu.cornell.jnutella.protocol.session;

import org.jboss.netty.channel.ChannelHandlerContext;

public abstract class SessionEvent {

  private final SessionModel model;
  private final ChannelHandlerContext context;

  public SessionEvent(SessionModel model, ChannelHandlerContext context) {
    super();
    this.model = model;
    this.context = context;
  }

  public SessionModel getModel() {
    return model;
  }

  public ChannelHandlerContext getContext() {
    return context;
  }
}
