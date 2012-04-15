package edu.cornell.jnutella.gnutella.messages.encoding;

import org.jboss.netty.buffer.ChannelBuffer;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import edu.cornell.jnutella.gnutella.messages.MessageBody;
import edu.cornell.jnutella.gnutella.messages.MessageHeader;
import edu.cornell.jnutella.gnutella.messages.QueryHitBody;
import edu.cornell.jnutella.gnutella.messages.ResponseBody;
import edu.cornell.jnutella.gnutella.messages.encoding.GGEPEncoder.EncoderInput;
import edu.cornell.jnutella.gnutella.session.ForMessageType;
import edu.cornell.jnutella.util.ByteUtils;

@ForMessageType(MessageHeader.F_QUERY_REPLY)
public class QueryHitEncoder implements MessageBodyEncoder {
  private final GGEPEncoder ggepEncoder;
  private final ResponseEncoder responseEncoder;

  @Inject
  public QueryHitEncoder(GGEPEncoder ggepEncoder, ResponseEncoder responseEncoder) {
    this.ggepEncoder = ggepEncoder;
    this.responseEncoder = responseEncoder;
  }

  public void encode(ChannelBuffer buffer, QueryHitBody toEncode) throws EncodingException {
    
    buffer.writeByte(toEncode.getNumHits());
    ByteUtils.short2leb((short) toEncode.getPort(), buffer);
    buffer.writeBytes(toEncode.getAddress().getAddress(), 0, 4);
    ByteUtils.int2leb((int) toEncode.getSpeed(), buffer);
    
    for (ResponseBody hit : toEncode.getHitList()){
      responseEncoder.encode(buffer, hit);
    }
      
    buffer.writeBytes(toEncode.getEQHD());
    
    if (toEncode.getGgep() != null) {
      EncoderInput ei = new EncoderInput(toEncode.getGgep(), true);
      ggepEncoder.encode(buffer, ei);
    }
    
    buffer.writeBytes(toEncode.getServantID().getBytes());

  }

  @Override
  public void encode(ChannelBuffer channel, MessageBody toEncode) throws EncodingException {
    Preconditions.checkArgument(toEncode instanceof QueryHitBody, "Not a QueryHit body.");
    encode(channel, (QueryHitBody) toEncode);
  }
}
