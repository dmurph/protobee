package org.protobee.gnutella.session;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.protobee.gnutella.messages.GnutellaMessage;
import org.protobee.session.SessionEvent;


public class MessageSendingEvent extends SessionEvent {

  private final GnutellaMessage message;

  public MessageSendingEvent(ChannelHandlerContext context,
      GnutellaMessage message) {
    super(context);
    this.message = message;
  }

  public GnutellaMessage getMessage() {
    return message;
  }
}
