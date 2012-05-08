package org.protobee.protocol.handlers;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.protobee.events.SessionClosingEvent;
import org.protobee.guice.scopes.SessionScope;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

/**
 * Posts a {@link SessionClosingEvent} to the event bus when the channel is disconnected
 * 
 * @author Daniel
 */
@SessionScope
public class SessionClosingPoster extends SimpleChannelUpstreamHandler {
  private final EventBus eventBus;

  @Inject
  public SessionClosingPoster(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  @Override
  public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    eventBus.post(new SessionClosingEvent(ctx));
  }
}
