package edu.cornell.jnutella.protocol.session;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelDownstreamHandler;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.slf4j.Logger;

import com.google.common.eventbus.EventBus;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import edu.cornell.jnutella.annotation.InjectLogger;

/**
 * Preconditions: modules in the model are already subscribed to the event bus and previously
 * filtered before sending first handshake, and are actively filtered by the upstream handler and
 * unregistered from the event bus
 * 
 * @author Daniel
 * 
 */
public class SessionDownstreamHandshaker extends SimpleChannelDownstreamHandler {

  public static interface Factory {
    SessionDownstreamHandshaker create(SessionModel session,
        ProtocolSessionBootstrapper protocolBootstrapper);
  }

  @InjectLogger
  private Logger log;
  private final SessionModel sessionModel;
  private final EventBus eventBus;
  private final ProtocolSessionBootstrapper bootstrapper;

  @AssistedInject
  public SessionDownstreamHandshaker(@Assisted SessionModel session,
      @Assisted ProtocolSessionBootstrapper bootstrapper) {
    this.sessionModel = session;
    this.eventBus = session.getEventBus();
    this.bootstrapper = bootstrapper;
  }

  @Override
  public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    if (!(e.getMessage() instanceof HttpMessage)) {
      log.error("Message is not an HttpMessage");
      throw new IllegalStateException("Message is not an HttpMessage");
    }

    if (sessionModel.getSessionState() == null) {
      log.debug("Session state is null, setting to h0");
      sessionModel.setSessionState(SessionState.HANDSHAKE_0);
    }

    if (sessionModel.getSessionState() == SessionState.MESSAGES) {
      log.error("Should have been removed from the pipeline before entering the message state");
      throw new IllegalStateException(
          "Should have been removed from the pipeline before entering the message state");
    }

    HttpMessage request = (HttpMessage) e.getMessage();

    // just post to the event bus because we do all filtering logic and setup in the upstream
    // handshaker
    eventBus.post(new HandshakeSendingEvent(sessionModel, ctx, request));

    Channels.write(ctx, e.getFuture(), request);

    switch (sessionModel.getSessionState()) {
      case HANDSHAKE_0:
        sessionModel.setSessionState(SessionState.HANDSHAKE_1);
        break;
      case HANDSHAKE_1:
        sessionModel.setSessionState(SessionState.HANDSHAKE_2);
        break;
      case HANDSHAKE_2:
        sessionModel.setSessionState(SessionState.MESSAGES);

        bootstrapper.bootstrapProtocolPipeline(ctx.getPipeline(), eventBus, sessionModel, ctx);
        break;
    }
  }
}
