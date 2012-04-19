package edu.cornell.jnutella.network;

import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpMessageEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.slf4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.guice.SessionScope;
import edu.cornell.jnutella.protocol.session.SessionModel;

@SessionScope
public class HandshakeHttpMessageEncoder implements ChannelDownstreamHandler {

  @InjectLogger
  private Logger log;
  private final HttpMessageEncoder requestEncoder;
  private final HttpResponseEncoder responseEncoder;
  private final SessionModel sessionModel;

  @Inject
  public HandshakeHttpMessageEncoder(@Named("request") HttpMessageEncoder requestEncoder,
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
        log.error("Cannot be in messages state");
        throw new IllegalStateException("Cannot be in messages state");
    }
  }
}
