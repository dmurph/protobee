package org.protobee.session.handshake;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.protobee.session.SessionEvent;

public class HandshakeReceivedEvent extends SessionEvent {

  private final HttpMessage message;
  private final HandshakeInterruptor interruptor;

  public HandshakeReceivedEvent(ChannelHandlerContext context, HttpMessage message,
      HandshakeInterruptor interruptor) {
    super(context);
    this.message = message;
    this.interruptor = interruptor;
  }

  public HttpMessage getMessage() {
    return message;
  }

  public HandshakeInterruptor getInterruptor() {
    return interruptor;
  }
}
