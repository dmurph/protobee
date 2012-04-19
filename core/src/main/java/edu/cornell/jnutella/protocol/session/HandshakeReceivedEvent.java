package edu.cornell.jnutella.protocol.session;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpMessage;

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
