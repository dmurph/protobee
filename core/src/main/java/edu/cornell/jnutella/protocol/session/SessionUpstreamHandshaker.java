package edu.cornell.jnutella.protocol.session;

import java.util.Map;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;

import com.google.common.base.Predicate;
import com.google.common.eventbus.EventBus;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.modules.ProtocolModule;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.protocol.headers.CompatabilityHeaderMerger;
import edu.cornell.jnutella.util.ProtocolModuleFilter;

/**
 * Preconditions: modules in session model are already subscribed to the event bus, and are mutable.
 * Session is at the correct initial state
 * 
 * @author Daniel
 * 
 */
public class SessionUpstreamHandshaker extends SimpleChannelUpstreamHandler {

  public static interface Factory {
    SessionUpstreamHandshaker create(SessionModel session, CompatabilityHeaderMerger headerMerger,
        ProtocolSessionBootstrapper bootstrapper);
  }

  @InjectLogger
  private Logger log;
  private final SessionModel sessionModel;
  private final CompatabilityHeaderMerger headerMerger;
  private final HandshakeInterruptor interruptor;
  private final EventBus eventBus;
  private final ProtocolModuleFilter filter;
  private final ProtocolSessionBootstrapper bootstrapper;

  @AssistedInject
  public SessionUpstreamHandshaker(@Assisted SessionModel session,
      @Assisted CompatabilityHeaderMerger headerMerger, HandshakeInterruptor interruptor,
      ProtocolModuleFilter filter, @Assisted ProtocolSessionBootstrapper bootstrapper) {
    this.sessionModel = session;
    this.headerMerger = headerMerger;
    this.interruptor = interruptor;
    this.filter = filter;
    this.eventBus = session.getEventBus();
    this.bootstrapper = bootstrapper;
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
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

    if (sessionModel.getSessionState() == SessionState.HANDSHAKE_2) {
      eventBus.post(new HandshakeReceivedEvent(sessionModel, ctx, request, null));
      sessionModel.setSessionState(SessionState.MESSAGES);

      bootstrapper.bootstrapProtocolPipeline(ctx.getPipeline(), eventBus, sessionModel, ctx);
      return;
    }

    // they initiated the connection, we just got their first message
    Map<String, String> mergedCompatabilityHeaders = headerMerger.mergeHeaders(request);
    filter.filterModules(sessionModel.getModules(), mergedCompatabilityHeaders,
        new Predicate<ProtocolModule>() {
          @Override
          public boolean apply(ProtocolModule input) {
            eventBus.unregister(input);
            return true;
          }
        });

    eventBus.post(new HandshakeReceivedEvent(sessionModel, ctx, request, interruptor));

    HttpResponseStatus interruptingResponse = interruptor.getInterruptingDisconnect();

    Protocol protocol = sessionModel.getProtocol();
    HttpVersion version =
        new HttpVersion(protocol.name(), protocol.majorVersion(), protocol.minorVersion(), true);

    HttpResponse response;
    if (interruptingResponse != null) {
      response = new DefaultHttpResponse(version, interruptingResponse);
    } else {
      response = new DefaultHttpResponse(version, new HttpResponseStatus(200, "OK"));
    }

    switch (sessionModel.getSessionState()) {
      case HANDSHAKE_0:
        for (String name : mergedCompatabilityHeaders.keySet()) {
          response.addHeader(name, mergedCompatabilityHeaders.get(name));
        }
        sessionModel.setSessionState(SessionState.HANDSHAKE_1);
        break;
      case HANDSHAKE_1:
        sessionModel.setSessionState(SessionState.HANDSHAKE_2);
        break;
    }

    Channels.write(ctx.getChannel(), response);
  }
}
