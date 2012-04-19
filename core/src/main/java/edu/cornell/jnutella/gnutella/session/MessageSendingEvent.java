package edu.cornell.jnutella.gnutella.session;

import org.jboss.netty.channel.ChannelHandlerContext;

import edu.cornell.jnutella.gnutella.messages.GnutellaMessage;
import edu.cornell.jnutella.protocol.session.SessionEvent;

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
