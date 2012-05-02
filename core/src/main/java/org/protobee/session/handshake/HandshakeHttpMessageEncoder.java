package org.protobee.session.handshake;

import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpMessageEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.protobee.annotation.InjectLogger;
import org.protobee.guice.scopes.SessionScope;
import org.protobee.protocol.RequestEncoding;
import org.protobee.session.SessionModel;
import org.slf4j.Logger;

import com.google.inject.Inject;

@SessionScope
public class HandshakeHttpMessageEncoder implements ChannelDownstreamHandler {

  @InjectLogger
  private Logger log;
  private final HttpMessageEncoder requestEncoder;
  private final HttpResponseEncoder responseEncoder;
  private final SessionModel sessionModel;

  @Inject
  public HandshakeHttpMessageEncoder(@RequestEncoding HttpMessageEncoder requestEncoder,
      HttpResponseEncoder responseEncoder, SessionModel sessionModel) {
    this.requestEncoder = requestEncoder;
    this.responseEncoder = responseEncoder;
    this.sessionModel = sessionModel;
  }

  @Override
  public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
    switch (sessionModel.getSessionState()) {
      case HANDSHAKE_0:
        requestEncoder.handleDownstream(ctx, e);
        break;
      case HANDSHAKE_1:
      case HANDSHAKE_2:
        responseEncoder.handleDownstream(ctx, e);
        break;
      case MESSAGES:
      default:
        log.error("Illegal session state");
        throw new IllegalStateException("Illegal session state");
    }
  }
}
