package edu.cornell.jnutella.gnutella.messages.encoding;

import java.util.Map;
import java.util.Set;

import org.jboss.netty.buffer.ChannelBuffer;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.gnutella.Gnutella;
import edu.cornell.jnutella.gnutella.messages.GnutellaMessage;
import edu.cornell.jnutella.gnutella.messages.MessageBody;
import edu.cornell.jnutella.gnutella.messages.MessageHeader;
import edu.cornell.jnutella.gnutella.session.ForMessageType;

public class GnutellaMessageEncoder implements PartEncoder<GnutellaMessage> {

  @InjectLogger
  private Logger log;
  private final MessageHeaderEncoder headerEncoder;
  private final Map<Byte, MessageBodyEncoder> messageEncoders;

  @Inject
  public GnutellaMessageEncoder(MessageHeaderEncoder headerEncoder,
      @Gnutella Set<MessageBodyEncoder> messageEncoders) {
    this.headerEncoder = headerEncoder;

    ImmutableMap.Builder<Byte, MessageBodyEncoder> decodersBuilder = ImmutableMap.builder();

    for (MessageBodyEncoder partDecoder : messageEncoders) {
      ForMessageType forMessageType = partDecoder.getClass().getAnnotation(ForMessageType.class);
      if (forMessageType == null) {
        throw new RuntimeException(
            "Registered body encoders must have the ForMessageType annotation");
      }
      byte value = forMessageType.value();
      decodersBuilder.put(value, partDecoder);
    }

    try {
      this.messageEncoders = decodersBuilder.build();
    } catch (IllegalArgumentException e) {
      log.error("Cannot have two messages encoders for the same message type", e);
      throw new IllegalArgumentException(
          "Cannot have two messages decoders for the same message type", e);
    }
  }

  @Override
  public void encode(ChannelBuffer channel, GnutellaMessage message) throws EncodingException {
    MessageHeader header = message.getHeader();
    MessageBody body = message.getBody();

    if (!messageEncoders.containsKey(header.getPayloadType())) {
      log.error("message encoder not found for message " + header);
      throw new EncodingException("message encoder not found for message " + header);
    }
    MessageBodyEncoder encoder = messageEncoders.get(header.getPayloadType());


    int startWriterPos = channel.writerIndex();
    int startBodyPos = startWriterPos + MessageHeader.MESSAGE_HEADER_LENGTH;
    // write body
    channel.writerIndex(startBodyPos);
    encoder.encode(channel, body);
    int endBodyPos = channel.writerIndex();
    // write header
    header.setPayloadLength(endBodyPos - startBodyPos);
    channel.writerIndex(startWriterPos);
    headerEncoder.encode(channel, header);
    if(channel.writerIndex() != startBodyPos) {
      throw new EncodingException("Encoded message header was not the correct size.");
    }
    channel.writerIndex(endBodyPos);
  }
}
