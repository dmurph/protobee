package edu.cornell.jnutella.messages.encoding;

import org.jboss.netty.buffer.ChannelBuffer;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import edu.cornell.jnutella.gnutella.session.ForMessageType;
import edu.cornell.jnutella.messages.MessageBody;
import edu.cornell.jnutella.messages.MessageHeader;
import edu.cornell.jnutella.messages.QueryBody;
import edu.cornell.jnutella.messages.encoding.GGEPEncoder.EncoderInput;
import edu.cornell.jnutella.util.ByteUtils;

@ForMessageType(MessageHeader.F_QUERY)
public class QueryEncoder implements MessageBodyEncoder {
  private final GGEPEncoder ggepEncoder;

  @Inject
  public QueryEncoder(GGEPEncoder ggepEncoder) {
    this.ggepEncoder = ggepEncoder;
  }

  public void encode(ChannelBuffer buffer, QueryBody toEncode) throws EncodingException {
    ByteUtils.short2leb((short) toEncode.getMinSpeed(), buffer);
    buffer.writeBytes(toEncode.getQuery().getBytes());
    buffer.writeByte(0);
    
    if (toEncode.getGgep() != null) {
      EncoderInput ei = new EncoderInput(toEncode.getGgep(), false);
      ggepEncoder.encode(buffer, ei);
    }
  }

  @Override
  public void encode(ChannelBuffer channel, MessageBody toEncode) throws EncodingException {
    Preconditions.checkArgument(toEncode instanceof QueryBody, "Not a Query body.");
    encode(channel, (QueryBody) toEncode);
  }
}