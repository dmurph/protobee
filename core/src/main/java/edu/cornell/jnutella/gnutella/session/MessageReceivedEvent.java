package edu.cornell.jnutella.gnutella.session;

import org.jboss.netty.channel.ChannelHandlerContext;

import edu.cornell.jnutella.gnutella.messages.GnutellaMessage;
import edu.cornell.jnutella.session.SessionEvent;

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
