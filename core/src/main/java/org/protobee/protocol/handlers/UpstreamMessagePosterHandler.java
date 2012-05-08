package org.protobee.protocol.handlers;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.protobee.identity.NetworkIdentity;
import org.protobee.protocol.ProtocolModel;
import org.protobee.session.SessionModel;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

/**
 * Handler that posts channel messages onto an {@link EventBus}.
 * 
 * @author Daniel
 */
public class UpstreamMessagePosterHandler extends SimpleChannelUpstreamHandler {

  public static interface Factory {
    /** Preconditions: in protocol, identity, and session scope */
    UpstreamMessagePosterHandler create(ChannelMessagePoster poster, FilterMode mode);
  }

  private final SessionModel session;
  private final NetworkIdentity identity;
  private final ProtocolModel protocol;
  private final FilterMode mode;
  private final ChannelMessagePoster poster;

  @AssistedInject
  public UpstreamMessagePosterHandler(@Assisted ChannelMessagePoster poster,
      @Assisted FilterMode mode, SessionModel session, NetworkIdentity identity,
      ProtocolModel protocol) {
    Preconditions.checkNotNull(mode);
    Preconditions.checkNotNull(session);
    Preconditions.checkNotNull(identity);
    Preconditions.checkNotNull(protocol);
    this.poster = poster;
    this.mode = mode;
    this.session = session;
    this.identity = identity;
    this.protocol = protocol;
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    Object message = e.getMessage();
    try {
      protocol.enterScope();
      identity.enterScope();
      session.enterScope();
      if (!poster.postEventForMessage(ctx, message)) {
        Preconditions.checkState(mode == FilterMode.SKIP_MISMATCHED_TYPES,
            "Writing message that doesn't match an event factory: " + message);
        super.messageReceived(ctx, e);
      }
    } finally {
      session.exitScope();
      identity.exitScope();
      protocol.exitScope();
    }
  }
}
