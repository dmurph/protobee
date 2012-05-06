package org.protobee.session;

import java.util.Set;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.protobee.events.ProtocolHandlersLoadedEvent;
import org.protobee.guice.scopes.SessionScope;
import org.protobee.protocol.HandshakeFuture;
import org.protobee.protocol.ProtocolChannelHandlers;
import org.protobee.session.handshake.HandshakeHandlers;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Removes handshake handlers and adds protocol handlers.
 * 
 * @author Daniel
 */
@SessionScope
public class ProtocolSessionBootstrapper {

  private final Provider<ChannelHandler[]> handlersProvider;
  private final Provider<ChannelFuture> handshakeComplete;
  private final Provider<Set<ChannelHandler>> handshakeHandlersToRemove;

  @Inject
  public ProtocolSessionBootstrapper(
      @HandshakeHandlers Provider<Set<ChannelHandler>> handshakeHandlersToRemove,
      @ProtocolChannelHandlers Provider<ChannelHandler[]> handlersProvider,
      @HandshakeFuture Provider<ChannelFuture> handshakeComplete) {
    this.handshakeHandlersToRemove = handshakeHandlersToRemove;
    this.handlersProvider = handlersProvider;
    this.handshakeComplete = handshakeComplete;
  }

  /**
   * Preconditions: in corresponding protocol, identity, and session scopes
   */
  public void bootstrapProtocolPipeline(ChannelPipeline pipeline, EventBus eventBus,
      SessionModel model, ChannelHandlerContext context) {
    // remove handshaker handlers
    Set<ChannelHandler> handlersToRemove = handshakeHandlersToRemove.get();
    for (ChannelHandler handler : handlersToRemove) {
      pipeline.remove(handler);
    }
    // add new handlers
    for (ChannelHandler handler : handlersProvider.get()) {
      pipeline.addLast(handler.toString(), handler);
    }
    ChannelFuture handshake = handshakeComplete.get();
    handshake.setSuccess();

    // post event
    eventBus.post(new ProtocolHandlersLoadedEvent(context));
  }
}
