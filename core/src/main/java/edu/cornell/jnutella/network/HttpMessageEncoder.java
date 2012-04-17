package edu.cornell.jnutella.network;

import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.slf4j.Logger;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.protocol.session.SessionModel;

public class HttpMessageEncoder implements ChannelDownstreamHandler {

  public static interface Factory {
    HttpMessageEncoder create(SessionModel sessionModel);
  }

  @InjectLogger
  private Logger log;
  private final HttpRequestEncoder requestEncoder;
  private final HttpResponseEncoder responseEncoder;
  private final SessionModel sessionModel;


  @AssistedInject
  public HttpMessageEncoder(HttpRequestEncoder requestEncoder, HttpResponseEncoder responseEncoder,
                            @Assisted SessionModel sessionModel) {
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
