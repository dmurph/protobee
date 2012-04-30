package org.protobee.gnutella.messages.encoding;

import org.jboss.netty.buffer.ChannelBuffer;
import org.protobee.gnutella.messages.MessageBody;
import org.protobee.gnutella.messages.MessageHeader;
import org.protobee.gnutella.messages.PushBody;
import org.protobee.gnutella.messages.encoding.GGEPEncoder.EncoderInput;
import org.protobee.gnutella.session.ForMessageType;
import org.protobee.util.ByteUtils;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;


@ForMessageType(MessageHeader.F_PUSH)
public class PushEncoder implements MessageBodyEncoder {
  private final GGEPEncoder ggepEncoder;
  
  @Inject
  public PushEncoder(GGEPEncoder ggepEncoder) {
    this.ggepEncoder = ggepEncoder;
  }

  public void encode(ChannelBuffer buffer, PushBody toEncode) throws EncodingException{
    Preconditions.checkArgument(toEncode.getServantID().length == 16);
    buffer.writeBytes(toEncode.getServantID());
    buffer.writerIndex(16);
    
    ByteUtils.int2leb((int) toEncode.getIndex(), buffer);
    buffer.writeBytes(toEncode.getAddress().getAddress(), 0, 4);
    ByteUtils.short2leb((short) toEncode.getPort(), buffer);
    
    if (toEncode.getGgep() != null) {
      EncoderInput ei = new EncoderInput(toEncode.getGgep(), false);
      ggepEncoder.encode(buffer, ei);
    }
  }

  @Override
  public void encode(ChannelBuffer channel, MessageBody toEncode) throws EncodingException {
    Preconditions.checkArgument(toEncode instanceof PushBody, "Not a Push body.");
    encode(channel, (PushBody) toEncode);
  }
}
