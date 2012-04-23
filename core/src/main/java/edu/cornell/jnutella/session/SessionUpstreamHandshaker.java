package edu.cornell.jnutella.session;

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
import com.google.inject.Inject;

import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.guice.SessionScope;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.modules.ProtocolModule;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.protocol.headers.CompatabilityHeaderMerger;
import edu.cornell.jnutella.util.ProtocolModuleFilter;

/**
 * 
 * @author Daniel
 */
@SessionScope
public class SessionUpstreamHandshaker extends SimpleChannelUpstreamHandler {

  @InjectLogger
  private Logger log;
  private final SessionModel sessionModel;
  private final ProtocolSessionModel protocolSessionModel;
  private final CompatabilityHeaderMerger headerMerger;
  private final HandshakeInterruptor interruptor;
  private final EventBus eventBus;
  private final ProtocolModuleFilter filter;
  private final ProtocolSessionBootstrapper bootstrapper;
  private final NetworkIdentity identity;
  private final Protocol protocol;

  @Inject
  public SessionUpstreamHandshaker(SessionModel session, CompatabilityHeaderMerger headerMerger,
      HandshakeInterruptor interruptor, ProtocolModuleFilter filter, EventBus eventBus,
      ProtocolSessionBootstrapper bootstrapper, NetworkIdentity identity, Protocol protocol,
      ProtocolSessionModel protocolSessionModel) {
    this.sessionModel = session;
    this.headerMerger = headerMerger;
    this.interruptor = interruptor;
    this.filter = filter;
    this.eventBus = eventBus;
    this.bootstrapper = bootstrapper;
    this.identity = identity;
    this.protocol = protocol;
    this.protocolSessionModel = protocolSessionModel;
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
      eventBus.post(new HandshakeReceivedEvent(ctx, request, null));
      bootstrapper.bootstrapProtocolPipeline(ctx.getPipeline(), eventBus, sessionModel, ctx);
      sessionModel.setSessionState(SessionState.MESSAGES);
      return;
    }

    identity.enterScope();
    sessionModel.enterScope();
    // they initiated the connection, we just got their first message
    Map<String, String> mergedCompatabilityHeaders = headerMerger.mergeHeaders(request);
    filter.filterModules(protocolSessionModel.getMutableModules(), mergedCompatabilityHeaders,
        new Predicate<ProtocolModule>() {
          @Override
          public boolean apply(ProtocolModule input) {
            eventBus.unregister(input);
            return true;
          }
        });

    eventBus.post(new HandshakeReceivedEvent(ctx, request, interruptor));

    HttpResponseStatus interruptingResponse = interruptor.getInterruptingDisconnect();

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
    sessionModel.exitScope();
    identity.exitScope();

    Channels.write(ctx.getChannel(), response);
  }
}
