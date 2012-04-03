package edu.cornell.jnutella.messages.decoding;

import java.util.Map;
import java.util.Set;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.slf4j.Logger;

import com.google.common.collect.Maps;
import com.google.inject.Inject;

import edu.cornell.jnutella.ConnectionManager;
import edu.cornell.jnutella.annotation.Gnutella;
import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.messages.GnutellaMessage;
import edu.cornell.jnutella.messages.MessageBody;
import edu.cornell.jnutella.messages.MessageHeader;
import edu.cornell.jnutella.network.FrameDecoderLE;
import edu.cornell.jnutella.network.ReplayingDecoderLE;
import edu.cornell.jnutella.session.gnutella.ForMessageType;
import edu.cornell.jnutella.session.gnutella.GnutellaSessionModel;
import edu.cornell.jnutella.session.gnutella.GnutellaSessionState;

/**
 * Handles the decoding of messages in the gnutella protocol. When the session state isn't at
 * messaging, this delegates decoding to an {@link HttpRequestDecoder} for the headers.
 * 
 * @author Daniel
 */
public class GnutellaDecoderHandler extends FrameDecoderLE {

  @InjectLogger
  private Logger log;
  private final ConnectionManager connectionManager;
  private final HttpRequestDecoder handshakeDecoder;
  private final MessageHeaderDecoder headerDecoder;
  private final Map<Byte, MessageBodyDecoder<?>> messageDecoders = Maps.newHashMap();

  private MessageHeader header;
  private MessageBodyDecoder<?> currentDecoder;

  @Inject
  public GnutellaDecoderHandler(HttpRequestDecoder handshakeDecoder,
      MessageHeaderDecoder headerDecoder, @Gnutella Set<MessageBodyDecoder<?>> messageDecoders,
      ConnectionManager connectionManager) {
    this.headerDecoder = headerDecoder;
    this.connectionManager = connectionManager;
    this.handshakeDecoder = handshakeDecoder;

    for (MessageBodyDecoder<?> partDecoder : messageDecoders) {
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
      handshakeDecoder.handleUpstream(ctx, e);
    } else {
      super.handleUpstream(ctx, e);
    }
  }

  @Override
  protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer)
      throws Exception {
    if (!buffer.readable()) {
      return null;
    }

    buffer.markReaderIndex();

    if (header == null) {
      header = headerDecoder.decode(buffer);
      if (header != null) {
        currentDecoder = messageDecoders.get(Byte.valueOf(header.getPayloadType()));
        if (currentDecoder == null) {
          log.error("decoder not found for message: " + header);

          // TODO: error? ignore message?
        }
        buffer.markReaderIndex();
      } else {
        buffer.resetReaderIndex();
      }
    } 
    
    if (header != null) {
      if (buffer.readableBytes() < header.getPayloadLength()) {
        return null;
      }

      MessageBody body = currentDecoder.decode(buffer);
      if (body == null) {
        log.error("decoder error for message " + header);
        // TODO: error? ignore message?
      }
      GnutellaMessage message = new GnutellaMessage(header, body);
      return message;
    }
    return null;
  }
}
