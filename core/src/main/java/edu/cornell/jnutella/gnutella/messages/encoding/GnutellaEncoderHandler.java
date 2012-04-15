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

import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.gnutella.Gnutella;
import edu.cornell.jnutella.gnutella.GnutellaIdentityModel;
import edu.cornell.jnutella.gnutella.messages.GnutellaMessage;
import edu.cornell.jnutella.gnutella.messages.MessageHeader;
import edu.cornell.jnutella.gnutella.session.ForMessageType;
import edu.cornell.jnutella.gnutella.session.GnutellaSessionModel;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.identity.NetworkIdentityManager;
import edu.cornell.jnutella.protocol.Protocol;

public class GnutellaEncoderHandler extends SimpleChannelDownstreamHandler {

  @InjectLogger
  private Logger log;

  private final NetworkIdentityManager identityManager;
  private final HttpRequestEncoder handshakeRequestEncoder;
  private final HttpResponseEncoder handshakeResponseEncoder;
  private final MessageHeaderEncoder headerEncoder;
  private final Map<Byte, MessageBodyEncoder> messageEncoders;
  private final Protocol protocol;

  private NetworkIdentity identity = null;
  private GnutellaIdentityModel identityModel = null;
  private GnutellaSessionModel sessionModel = null;

  @Inject
  public GnutellaEncoderHandler(HttpRequestEncoder handshakeRequestEncoder,
      HttpResponseEncoder handshakeResponseEncoder, MessageHeaderEncoder headerEncoder,
      NetworkIdentityManager identityManager, @Gnutella Set<MessageBodyEncoder> messageEncoders,
      @Gnutella Protocol protocol) {
    this.protocol = protocol;
    this.identityManager = identityManager;
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

    if (identity == null) {
      identity = identityManager.getNewtorkIdentity(channel.getRemoteAddress());
      identityModel = (GnutellaIdentityModel) identity.getModel(protocol);
      sessionModel = identityModel.getCurrentSession();
    }

    switch (sessionModel.getState()) {
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
