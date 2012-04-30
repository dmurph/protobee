package org.protobee.session.handshake;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpMessageDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.protobee.annotation.InjectLogger;
import org.protobee.guice.SessionScope;
import org.protobee.session.SessionModel;
import org.slf4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;


@SessionScope
public class HandshakeHttpMessageDecoder implements ChannelUpstreamHandler {

  @InjectLogger
  private Logger log;
  private final HttpMessageDecoder requestDecoder;
  private final HttpResponseDecoder responseDecoder;
  private final SessionModel sessionModel;


  @Inject
  public HandshakeHttpMessageDecoder(@Named("request") HttpMessageDecoder requestDecoder,
      HttpResponseDecoder responseDecoder, SessionModel sessionModel) {
    this.requestDecoder = requestDecoder;
    this.responseDecoder = responseDecoder;
    this.sessionModel = sessionModel;
  }

  @Override
  public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
    switch (sessionModel.getSessionState()) {
      case HANDSHAKE_0:
        requestDecoder.handleUpstream(ctx, e);
        break;
      case HANDSHAKE_1:
      case HANDSHAKE_2:
        responseDecoder.handleUpstream(ctx, e);
        break;
      case MESSAGES:
        log.error("Cannot be in messages state");
        throw new IllegalStateException("Cannot be in messages state");
    }
  }
}
