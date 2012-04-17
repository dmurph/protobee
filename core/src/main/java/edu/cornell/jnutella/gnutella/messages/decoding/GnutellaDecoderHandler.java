package edu.cornell.jnutella.gnutella.messages.decoding;

import java.util.Map;
import java.util.Set;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.gnutella.Gnutella;
import edu.cornell.jnutella.gnutella.GnutellaIdentityModel;
import edu.cornell.jnutella.gnutella.messages.GnutellaMessage;
import edu.cornell.jnutella.gnutella.messages.MessageBody;
import edu.cornell.jnutella.gnutella.messages.MessageHeader;
import edu.cornell.jnutella.gnutella.session.ForMessageType;
import edu.cornell.jnutella.gnutella.session.GnutellaSessionModel;
import edu.cornell.jnutella.gnutella.session.GnutellaSessionState;
import edu.cornell.jnutella.identity.NetworkIdentity;
import edu.cornell.jnutella.identity.NetworkIdentityManager;
import edu.cornell.jnutella.network.FrameDecoderLE;
import edu.cornell.jnutella.protocol.Protocol;

/**
 * Handles the decoding of messages in the gnutella protocol. When the session state isn't at
 * messaging, this delegates decoding to an {@link HttpRequestDecoder} for the headers.
 * 
 * @author Daniel
 */
public class GnutellaDecoderHandler extends FrameDecoderLE {

  @InjectLogger
  private Logger log;
  private final NetworkIdentityManager identityManager;
  private final HttpRequestDecoder handshakeRequestDecoder;
  private final HttpResponseDecoder handshakeResponseDecoder;
  private final MessageHeaderDecoder headerDecoder;
  private final Map<Byte, MessageBodyDecoder<?>> messageDecoders;
  private final Protocol protocol;

  private NetworkIdentity identity = null;
  private GnutellaIdentityModel identityModel = null;
  private GnutellaSessionModel sessionModel = null;

  private MessageHeader header;
  private MessageBodyDecoder<?> currentDecoder;

  @Inject
  public GnutellaDecoderHandler(HttpRequestDecoder handshakeRequestDecoder,
      HttpResponseDecoder handshakeResponseDecoder, MessageHeaderDecoder headerDecoder,
      @Gnutella Set<MessageBodyDecoder<?>> messageDecoders, NetworkIdentityManager identityManager,
      @Gnutella Protocol protocol) {
    this.protocol = protocol;
    this.headerDecoder = headerDecoder;
    this.identityManager = identityManager;
    this.handshakeRequestDecoder = handshakeRequestDecoder;
    this.handshakeResponseDecoder = handshakeResponseDecoder;

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
  public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
    Channel channel = e.getChannel();
    if (identity == null) {
      identity = identityManager.getNewtorkIdentity(channel.getRemoteAddress());
      identityModel = (GnutellaIdentityModel) identity.getModel(protocol);
      sessionModel = identityModel.getCurrentSession();
    }
    
    if(sessionModel.getState() == null) {
      log.info("Receiving new session from host " + e.getChannel().getRemoteAddress());
      sessionModel.setState(GnutellaSessionState.HANDSHAKE_0);
    }

    switch (sessionModel.getState()) {
      case HANDSHAKE_0:
        handshakeRequestDecoder.handleUpstream(ctx, e);
        break;
      case HANDSHAKE_1:
      case HANDSHAKE_2:
        handshakeResponseDecoder.handleUpstream(ctx, e);
        break;
      case MESSAGES:
        super.handleUpstream(ctx, e);
        break;
      default:
        throw new RuntimeException("illegal state");
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
}
