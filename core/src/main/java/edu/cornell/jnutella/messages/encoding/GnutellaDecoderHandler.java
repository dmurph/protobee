package edu.cornell.jnutella.messages.encoding;

import java.awt.HeadlessException;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.slf4j.Logger;

import com.google.common.collect.Maps;
import com.google.inject.Inject;

import edu.cornell.jnutella.ConnectionManager;
import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.network.FrameDecoderLE;
import edu.cornell.jnutella.session.SessionModel;
import edu.cornell.jnutella.session.gnutella.ForMessageType;
import edu.cornell.jnutella.session.gnutella.GnutellaSessionModel;
import edu.cornell.jnutella.session.gnutella.GnutellaSessionState;

public class GnutellaDecoderHandler extends FrameDecoderLE {

  @InjectLogger
  private Logger log;
  private final ConnectionManager connectionManager;
  private HttpRequestDecoder headerDecoder;
  private Map<Byte, PartDecoder<?>> messageDecoders = Maps.newHashMap();

  private final ChannelBuffer buf = ChannelBuffers.dynamicBuffer();
  
  @Inject
  public GnutellaDecoderHandler(HttpRequestDecoder handshakeDecoder,
      Set<PartDecoder<?>> messageDecoders, ConnectionManager connectionManager) {
    this.connectionManager = connectionManager;
    this.headerDecoder = handshakeDecoder;

    for (PartDecoder<?> partDecoder : messageDecoders) {
      ForMessageType forMessageType = partDecoder.getClass().getAnnotation(ForMessageType.class);
      if (forMessageType == null) {
        throw new RuntimeException(
            "Registered part decoders must have the ForMessageType annotation");
      }
      byte value = forMessageType.value();
      if (this.messageDecoders.containsKey(value)) {
        log.error("Message decoder for message '" + value + "' already present");
      }
      this.messageDecoders.put(value, partDecoder);
    }
  }

  @Override
  public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
    GnutellaSessionModel session =
        (GnutellaSessionModel) connectionManager.getSessionModel(e.getChannel().getRemoteAddress());

    if (session.getState() != GnutellaSessionState.MESSAGES) {
      headerDecoder.handleUpstream(ctx, e);
    } else {
      super.handleUpstream(ctx, e);
    }
  }

  @Override
  protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer)
      throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
}
