package edu.cornell.jnutella.session;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;

import com.google.common.eventbus.EventBus;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import edu.cornell.jnutella.guice.SessionScope;
import edu.cornell.jnutella.protocol.ProtocolConfig;

/**
 * Removes handshake handlers and adds protocol handlers.
 * 
 * @author Daniel
 */
@SessionScope
public class ProtocolSessionBootstrapper {

  public static interface Factory {
    ProtocolSessionBootstrapper create(ChannelHandler[] handshakeHandlers);
  }

  private final ChannelHandler[] handshakeHandlers;
  private final ProtocolConfig config;

  @AssistedInject
  public ProtocolSessionBootstrapper(@Assisted ChannelHandler[] handshakeHandlers,
      ProtocolConfig config) {
    this.handshakeHandlers = handshakeHandlers;
    this.config = config;
  }

  public void bootstrapProtocolPipeline(ChannelPipeline pipeline, EventBus eventBus,
      SessionModel model, ChannelHandlerContext context) {
    // add new handlers
    for (ChannelHandler handler : config.createProtocolHandlers()) {
      pipeline.addLast(handler.toString(), handler);
    }
    // remove handshaker handlers
    for (ChannelHandler handler : handshakeHandlers) {
      pipeline.remove(handler);
    }

    // post event
    eventBus.post(new ProtocolHandlersLoadedEvent(context));
  }
}
