package org.protobee.session;

import java.util.Set;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.protobee.guice.scopes.SessionScope;
import org.protobee.protocol.HandshakeFuture;
import org.protobee.protocol.ProtocolConfig;
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

  private final ProtocolConfig config;
  private final Provider<ChannelFuture> handshakeComplete;
  private final Provider<Set<ChannelHandler>> handshakeHandlersToRemove;

  @Inject
  public ProtocolSessionBootstrapper(
      @HandshakeHandlers Provider<Set<ChannelHandler>> handshakeHandlersToRemove,
      ProtocolConfig config, @HandshakeFuture Provider<ChannelFuture> handshakeComplete) {
    this.handshakeHandlersToRemove = handshakeHandlersToRemove;
    this.config = config;
    this.handshakeComplete = handshakeComplete;
  }

  public void bootstrapProtocolPipeline(ChannelPipeline pipeline, EventBus eventBus,
      SessionModel model, ChannelHandlerContext context) {
    // add new handlers
    for (ChannelHandler handler : config.createProtocolHandlers()) {
      pipeline.addLast(handler.toString(), handler);
    }
    // remove handshaker handlers
    Set<ChannelHandler> handlersToRemove = handshakeHandlersToRemove.get();
    for (ChannelHandler handler : handlersToRemove) {
      pipeline.remove(handler);
    }
    ChannelFuture handshake = handshakeComplete.get();
    handshake.setSuccess();

    // post event
    eventBus.post(new ProtocolHandlersLoadedEvent(context));
  }
}
