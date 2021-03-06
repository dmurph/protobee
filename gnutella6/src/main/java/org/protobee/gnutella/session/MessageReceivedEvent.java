package org.protobee.gnutella.session;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.protobee.events.SessionEvent;
import org.protobee.gnutella.messages.GnutellaMessage;


public class MessageReceivedEvent extends SessionEvent {

  private final GnutellaMessage message;

  public MessageReceivedEvent(ChannelHandlerContext context,
      GnutellaMessage message) {
    super(context);
    this.message = message;
  }

  public GnutellaMessage getMessage() {
    return message;
  }
}
