package org.protobee.gnutella.messages.decoding;

import java.util.Map;
import java.util.Set;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.protobee.annotation.InjectLogger;
import org.protobee.gnutella.Gnutella;
import org.protobee.gnutella.messages.GnutellaMessage;
import org.protobee.gnutella.messages.MessageBody;
import org.protobee.gnutella.messages.MessageHeader;
import org.protobee.gnutella.session.ForMessageType;
import org.protobee.guice.scopes.SessionScope;
import org.protobee.network.handlers.FrameDecoderLE;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;


/**
 * Handles the decoding of messages in the gnutella protocol. When the session state isn't at
 * messaging, this delegates decoding to an {@link HttpRequestDecoder} for the headers.
 * 
 * @author Daniel
 */
@SessionScope
public class GnutellaDecoderHandler extends FrameDecoderLE {

  @InjectLogger
  private Logger log;
  private final MessageHeaderDecoder headerDecoder;
  private final Map<Byte, MessageBodyDecoder<?>> messageDecoders;

  private MessageHeader header;
  private MessageBodyDecoder<?> currentDecoder;

  @Inject
  public GnutellaDecoderHandler(MessageHeaderDecoder headerDecoder,
      @SuppressWarnings("rawtypes") @Gnutella Set<MessageBodyDecoder> messageDecoders) {
    this.headerDecoder = headerDecoder;

    ImmutableMap.Builder<Byte, MessageBodyDecoder<?>> decodersBuilder = ImmutableMap.builder();

    for (MessageBodyDecoder<?> partDecoder : messageDecoders) {
      ForMessageType forMessageType = partDecoder.getClass().getAnnotation(ForMessageType.class);
      if (forMessageType == null) {
        throw new RuntimeException(
            "Registered body decoders must have the ForMessageType annotation");
      }
      byte value = forMessageType.value();
      decodersBuilder.put(value, partDecoder);
    }

    try {
      this.messageDecoders = decodersBuilder.build();
    } catch (IllegalArgumentException e) {
      log.error("Cannot have two messages decoders for the same message type", e);
      throw new IllegalArgumentException(
          "Cannot have two messages decoders for the same message type", e);
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
          throw new DecodingException("decoder not found for message: " + header);
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
        log.error("decoder returned null for message " + header);
        throw new DecodingException("decoder returned null for message " + header);
      }
      GnutellaMessage message = new GnutellaMessage(header, body);
      return message;
    }
    return null;
  }

  @Override
  public String toString() {
    return "Gnutella Decoder Handler";
  }
  
  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
    log.error("Exception caught", e);
    super.exceptionCaught(ctx, e);
  }
}
