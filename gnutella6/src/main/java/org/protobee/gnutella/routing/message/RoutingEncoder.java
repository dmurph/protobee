package org.protobee.gnutella.routing.message;

import org.jboss.netty.buffer.ChannelBuffer;
import org.protobee.gnutella.messages.MessageBody;
import org.protobee.gnutella.messages.MessageHeader;
import org.protobee.gnutella.messages.encoding.EncodingException;
import org.protobee.gnutella.messages.encoding.MessageBodyEncoder;
import org.protobee.gnutella.session.ForMessageType;
import org.protobee.util.ByteUtils;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;


@ForMessageType(MessageHeader.F_ROUTE_TABLE_UPDATE)
public class RoutingEncoder implements MessageBodyEncoder {

  @Inject
  public RoutingEncoder() { }

  public void encode(ChannelBuffer buffer, RoutingBody toEncode) throws EncodingException {
    
    if (toEncode instanceof ResetBody){
      buffer.writeByte(((ResetBody) toEncode).getVariant());
      ByteUtils.int2leb((int) ((ResetBody) toEncode).getTableLength(), buffer);
      buffer.writeByte(((ResetBody) toEncode).getInfinity());
    }
    else if (toEncode instanceof PatchBody){
      buffer.writeByte(((PatchBody) toEncode).getVariant());
      buffer.writeByte(((PatchBody) toEncode).getSequenceNum());
      buffer.writeByte(((PatchBody) toEncode).getSequenceSize());
      buffer.writeByte(((PatchBody) toEncode).getCompressor());
      buffer.writeByte(((PatchBody) toEncode).getEntryBits());
      buffer.writeBytes(((PatchBody) toEncode).getData());
    }
    else{
      throw new EncodingException("Not a ResetBody or a PatchBody.");
    }
    
  }

  @Override
  public void encode(ChannelBuffer channel, MessageBody toEncode) throws EncodingException {
    Preconditions.checkArgument(toEncode instanceof RoutingBody, "Not a Routing body.");
    encode(channel, (RoutingBody) toEncode);
  }
}
