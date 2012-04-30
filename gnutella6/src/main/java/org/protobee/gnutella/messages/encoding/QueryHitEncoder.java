package org.protobee.gnutella.messages.encoding;

import org.jboss.netty.buffer.ChannelBuffer;
import org.protobee.gnutella.messages.MessageBody;
import org.protobee.gnutella.messages.MessageHeader;
import org.protobee.gnutella.messages.QueryHitBody;
import org.protobee.gnutella.messages.ResponseBody;
import org.protobee.gnutella.messages.encoding.GGEPEncoder.EncoderInput;
import org.protobee.gnutella.session.ForMessageType;
import org.protobee.util.ByteUtils;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;


@ForMessageType(MessageHeader.F_QUERY_REPLY)
public class QueryHitEncoder implements MessageBodyEncoder {
  private final GGEPEncoder ggepEncoder;
  private final ResponseEncoder responseEncoder;
  private final EQHDEncoder eqhdEncoder;

  @Inject
  public QueryHitEncoder(GGEPEncoder ggepEncoder, ResponseEncoder responseEncoder, EQHDEncoder eqhdEncoder) {
    this.ggepEncoder = ggepEncoder;
    this.responseEncoder = responseEncoder;
    this.eqhdEncoder = eqhdEncoder;
  }

  public void encode(ChannelBuffer buffer, QueryHitBody toEncode) throws EncodingException {
    
    buffer.writeByte(toEncode.getNumHits());
    ByteUtils.short2leb((short) toEncode.getPort(), buffer);
    buffer.writeBytes(toEncode.getAddress().getAddress(), 0, 4);
    ByteUtils.int2leb((int) toEncode.getSpeed(), buffer);
    
    for (ResponseBody hit : toEncode.getHitList()){
      responseEncoder.encode(buffer, hit);
    }
      
    eqhdEncoder.encode(buffer,  toEncode.getEqhd());
    
    buffer.writeBytes(toEncode.getPrivateArea1());
    
    if (toEncode.getGgep() != null) {
      EncoderInput ei = new EncoderInput(toEncode.getGgep(), true);
      ggepEncoder.encode(buffer, ei);
    }
    
    buffer.writeBytes(toEncode.getPrivateArea2());
    buffer.writeBytes(toEncode.getServantID());

  }

  @Override
  public void encode(ChannelBuffer channel, MessageBody toEncode) throws EncodingException {
    Preconditions.checkArgument(toEncode instanceof QueryHitBody, "Not a QueryHit body.");
    encode(channel, (QueryHitBody) toEncode);
  }
}
