package org.protobee.session.handshake;

import java.net.SocketAddress;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.protobee.annotation.InjectLogger;
import org.protobee.compatability.ModuleCompatabilityVersionMerger;
import org.protobee.events.HandshakeReceivedEvent;
import org.protobee.guice.scopes.SessionScope;
import org.protobee.identity.NetworkIdentity;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.ProtocolModel;
import org.protobee.session.ProtocolSessionBootstrapper;
import org.protobee.session.SessionModel;
import org.protobee.session.SessionState;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;


/**
 * 
 * @author Daniel
 */
@SessionScope
public class SessionUpstreamHandshaker extends SimpleChannelUpstreamHandler {

  @InjectLogger
  private Logger log;
  private final SessionModel sessionModel;
  private final ModuleCompatabilityVersionMerger headerMerger;
  private final HandshakeInterruptor interruptor;
  private final EventBus eventBus;
  private final ProtocolModuleFilter filter;
  private final ProtocolSessionBootstrapper bootstrapper;
  private final NetworkIdentity identity;
  private final ProtocolModel protocolModel;

  @Inject
  public SessionUpstreamHandshaker(SessionModel session,
      ModuleCompatabilityVersionMerger headerMerger, HandshakeInterruptor interruptor,
      ProtocolModuleFilter filter, EventBus eventBus, ProtocolSessionBootstrapper bootstrapper,
      NetworkIdentity identity, ProtocolModel protocolModel) {
    this.sessionModel = session;
    this.headerMerger = headerMerger;
    this.interruptor = interruptor;
    this.filter = filter;
    this.eventBus = eventBus;
    this.bootstrapper = bootstrapper;
    this.identity = identity;
    this.protocolModel = protocolModel;
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

    Protocol protocol = protocolModel.getProtocol();

    HttpResponse response;
    try {
      protocolModel.enterScope();
      identity.enterScope();
      sessionModel.enterScope();

      if (sessionModel.getSessionState() == SessionState.HANDSHAKE_2) {
        log.info("Resulting filtered modules: " + filter.getFilterModulesString());
        eventBus.post(new HandshakeReceivedEvent(ctx, request, null));
        bootstrapper.bootstrapProtocolPipeline(ctx.getPipeline(), eventBus, sessionModel, ctx);
        sessionModel.setSessionState(SessionState.MESSAGES);

        sessionModel.exitScope();
        identity.exitScope();
        protocolModel.exitScope();
        return;
      }

      // they initiated the connection, we just got their first message
      Map<String, String> mergedCompatabilityHeaders =
          headerMerger.mergeHandshakeHeaders(request, sessionModel.getSessionState());
      filter.filterModules(mergedCompatabilityHeaders);

      eventBus.post(new HandshakeReceivedEvent(ctx, request, interruptor));

      Set<HttpResponseStatus> interruptingResponse = interruptor.getDisconnectionInterrupts();
      interruptor.clear();

      HttpVersion version =
          new HttpVersion(protocol.name(), protocol.majorVersion(), protocol.minorVersion(), true);

      if (interruptingResponse.size() == 0) {
        response = new DefaultHttpResponse(version, new HttpResponseStatus(200, "OK"));
      } else {

        // this is just to log all the reasons
        if (log.isInfoEnabled()) {
          StringBuilder reasons = new StringBuilder();
          reasons.append("disconnect reasons from interruptor: {");
          boolean first = true;
          for (HttpResponseStatus httpResponseStatus : interruptingResponse) {
            if (first) {
              reasons.append("\"").append(httpResponseStatus.toString()).append("\"");
              first = false;
              continue;
            }
            reasons.append(", \"").append(httpResponseStatus.toString()).append("\"");
          }
          reasons.append("}");
          log.info(reasons.toString());
        }

        // just grab the first one
        response = new DefaultHttpResponse(version, interruptingResponse.iterator().next());
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

    } finally {
      sessionModel.exitScope();
      identity.exitScope();
      protocolModel.exitScope();
    }

    SocketAddress address = identity.getListeningAddress(protocol);

    Preconditions.checkState(address != null, "Listening address for identity " + identity
        + " with protocol " + protocol + " is null");

    Channels.write(ctx.getChannel(), response, address);
  }
}
