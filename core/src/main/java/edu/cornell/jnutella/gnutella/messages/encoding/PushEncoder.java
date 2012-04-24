package edu.cornell.jnutella.gnutella.messages.encoding;

import org.jboss.netty.buffer.ChannelBuffer;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import edu.cornell.jnutella.gnutella.messages.MessageBody;
import edu.cornell.jnutella.gnutella.messages.MessageHeader;
import edu.cornell.jnutella.gnutella.messages.PushBody;
import edu.cornell.jnutella.gnutella.messages.encoding.GGEPEncoder.EncoderInput;
import edu.cornell.jnutella.gnutella.session.ForMessageType;
import edu.cornell.jnutella.util.ByteUtils;

@ForMessageType(MessageHeader.F_PUSH)
public class PushEncoder implements MessageBodyEncoder {
  private final GGEPEncoder ggepEncoder;
  
  @Inject
  public PushEncoder(GGEPEncoder ggepEncoder) {
    this.ggepEncoder = ggepEncoder;
  }

  public void encode(ChannelBuffer buffer, PushBody toEncode) throws EncodingException{
    Preconditions.checkArgument(toEncode.getServantID().getBytes().length == 16);
    buffer.writeBytes(toEncode.getServantID().getBytes());
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
