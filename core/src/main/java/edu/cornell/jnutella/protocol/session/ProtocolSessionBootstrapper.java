package edu.cornell.jnutella.protocol.session;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;

import com.google.common.eventbus.EventBus;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

/**
 * Removes handshake handlers and adds protocol handlers.
 * 
 * @author Daniel
 */
public class ProtocolSessionBootstrapper {

  public static interface Factory {
    ProtocolSessionBootstrapper create(@Assisted("handshake") ChannelHandler[] handshakeHandlers,
        @Assisted("protocol") ChannelHandler[] protocolHandlers);
  }

  private final ChannelHandler[] handshakeHandlers;
  private final ChannelHandler[] protocolHandlers;

  @AssistedInject
  public ProtocolSessionBootstrapper(@Assisted("handshake") ChannelHandler[] handshakeHandlers,
      @Assisted("protocol") ChannelHandler[] protocolHandlers) {
    this.handshakeHandlers = handshakeHandlers;
    this.protocolHandlers = protocolHandlers;
  }

  public void bootstrapProtocolPipeline(ChannelPipeline pipeline, EventBus eventBus,
      SessionModel model, ChannelHandlerContext context) {
    // add new handlers
    for (ChannelHandler handler : protocolHandlers) {
      pipeline.addLast(handler.toString(), handler);
    }
    // remove handshaker handlers
    for (ChannelHandler handler : handshakeHandlers) {
      pipeline.remove(handler);
    }

    // post event
    eventBus.post(new ProtocolHandlersLoadedEvent(model, context));
  }
}
