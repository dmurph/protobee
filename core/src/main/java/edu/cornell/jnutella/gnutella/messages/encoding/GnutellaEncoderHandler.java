package edu.cornell.jnutella.gnutella.messages.encoding;

import java.nio.ByteOrder;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelDownstreamHandler;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import edu.cornell.jnutella.ConnectionKey;
import edu.cornell.jnutella.ConnectionManager;
import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.gnutella.Gnutella;
import edu.cornell.jnutella.gnutella.messages.GnutellaMessage;
import edu.cornell.jnutella.gnutella.messages.MessageHeader;
import edu.cornell.jnutella.gnutella.session.ForMessageType;
import edu.cornell.jnutella.gnutella.session.GnutellaSessionModel;
import edu.cornell.jnutella.session.SessionModel;

public class GnutellaEncoderHandler extends SimpleChannelDownstreamHandler {

  @InjectLogger
  private Logger log;

  private final ConnectionManager connectionManager;
  private final HttpRequestEncoder handshakeRequestEncoder;
  private final HttpResponseEncoder handshakeResponseEncoder;
  private final MessageHeaderEncoder headerEncoder;
  private final Map<Byte, MessageBodyEncoder> messageEncoders;

  @Inject
  public GnutellaEncoderHandler(HttpRequestEncoder handshakeRequestEncoder,
      HttpResponseEncoder handshakeResponseEncoder, MessageHeaderEncoder headerEncoder,
      ConnectionManager connectionManager, @Gnutella Set<MessageBodyEncoder> messageEncoders) {
    this.connectionManager = connectionManager;
    this.handshakeRequestEncoder = handshakeRequestEncoder;
    this.handshakeResponseEncoder = handshakeResponseEncoder;
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
  public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
    Channel channel = e.getChannel();
    SessionModel model =
        connectionManager.getSession(new ConnectionKey(channel.getLocalAddress(), channel
            .getRemoteAddress()));

    if (!(model instanceof GnutellaSessionModel)) {
      log.error("Not Gnutella session model: " + model);
      throw new IllegalStateException("Not Gnutella session model: " + model);
    }

    GnutellaSessionModel session = (GnutellaSessionModel) model;

    switch (session.getState()) {
      case HANDSHAKE_0:
        handshakeRequestEncoder.handleDownstream(ctx, e);
        break;
      case HANDSHAKE_1:
      case HANDSHAKE_2:
        handshakeResponseEncoder.handleDownstream(ctx, e);
        break;
      case MESSAGES:
        super.handleDownstream(ctx, e);
        break;
      default:
        throw new RuntimeException("illegal state");
    }
  }

  @Override
  public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    Preconditions.checkArgument(e.getMessage() instanceof GnutellaMessage,
        "Did not get a gnutella message");
    GnutellaMessage message = (GnutellaMessage) e.getMessage();
    MessageHeader header = message.getHeader();

    if (messageEncoders.containsKey(header.getPayloadType())) {
      log.error("message encoder not found for message " + header);
      throw new EncodingException("message encoder not found for message " + header);
    }

    ChannelBuffer messageBuffer =
        ChannelBuffers.buffer(ByteOrder.LITTLE_ENDIAN, MessageHeader.MESSAGE_HEADER_LENGTH
            + message.getHeader().getPayloadLength());

    MessageBodyEncoder encoder = messageEncoders.get(header.getPayloadType());

    headerEncoder.encode(messageBuffer, message.getHeader());
    encoder.encode(messageBuffer, message.getBody());

    Channels.write(ctx, e.getFuture(), messageBuffer);
  }
}
