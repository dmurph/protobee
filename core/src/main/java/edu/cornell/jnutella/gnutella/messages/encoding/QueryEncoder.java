package edu.cornell.jnutella.gnutella.messages.encoding;

import java.nio.charset.Charset;

import org.jboss.netty.buffer.ChannelBuffer;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import edu.cornell.jnutella.gnutella.messages.MessageBody;
import edu.cornell.jnutella.gnutella.messages.MessageHeader;
import edu.cornell.jnutella.gnutella.messages.QueryBody;
import edu.cornell.jnutella.gnutella.messages.encoding.GGEPEncoder.EncoderInput;
import edu.cornell.jnutella.gnutella.session.ForMessageType;
import edu.cornell.jnutella.util.ByteUtils;

@ForMessageType(MessageHeader.F_QUERY)
public class QueryEncoder implements MessageBodyEncoder {
  private final GGEPEncoder ggepEncoder;
  private final HUGEEncoder hugeEncoder;

  @Inject
  public QueryEncoder(GGEPEncoder ggepEncoder, HUGEEncoder hugeEncoder) {
    this.ggepEncoder = ggepEncoder;
    this.hugeEncoder = hugeEncoder;
  }

  public void encode(ChannelBuffer buffer, QueryBody toEncode) throws EncodingException {
    ByteUtils.short2leb((short) toEncode.getMinSpeed(), buffer);
    buffer.writeBytes(toEncode.getQuery().getBytes(Charset.forName("UTF-8")));
    buffer.writeByte(0);

    if (toEncode.getHuge() != null) {
      hugeEncoder.encode(buffer, toEncode.getHuge());
    }
    
    if (toEncode.getGgep() != null) {
      buffer.writeByte((byte) 0x1C);
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
