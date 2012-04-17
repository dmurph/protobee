package edu.cornell.jnutella.protocol.session;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpMessage;

public class HandshakeSendingEvent extends SessionEvent {

  private final HttpMessage message;

  public HandshakeSendingEvent(SessionModel model, ChannelHandlerContext context,
      HttpMessage message) {
    super(model, context);
    this.message = message;
  }

  public HttpMessage getMessage() {
    return message;
  }
}
