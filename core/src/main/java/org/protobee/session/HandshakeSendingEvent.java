package org.protobee.session;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpMessage;

public class HandshakeSendingEvent extends SessionEvent {

  private final HttpMessage message;

  public HandshakeSendingEvent(ChannelHandlerContext context, HttpMessage message) {
    super(context);
    this.message = message;
  }

  public HttpMessage getMessage() {
    return message;
  }
}
