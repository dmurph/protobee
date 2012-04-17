package edu.cornell.jnutella.network;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.slf4j.Logger;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.protocol.session.SessionModel;

public class HttpMessageDecoder implements ChannelUpstreamHandler {

  public static interface Factory {
    HttpMessageDecoder create(SessionModel sessionModel);
  }

  @InjectLogger
  private Logger log;
  private final HttpRequestDecoder requestDecoder;
  private final HttpResponseDecoder responseDecoder;
  private final SessionModel sessionModel;


  @AssistedInject
  public HttpMessageDecoder(HttpRequestDecoder requestDecoder, HttpResponseDecoder responseDecoder,
      @Assisted SessionModel sessionModel) {
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
