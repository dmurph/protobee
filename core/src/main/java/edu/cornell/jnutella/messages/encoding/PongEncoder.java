package edu.cornell.jnutella.messages.encoding;

import org.jboss.netty.buffer.ChannelBuffer;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import edu.cornell.jnutella.gnutella.session.ForMessageType;
import edu.cornell.jnutella.messages.MessageBody;
import edu.cornell.jnutella.messages.MessageHeader;
import edu.cornell.jnutella.messages.PongBody;
import edu.cornell.jnutella.messages.encoding.GGEPEncoder.EncoderInput;
import edu.cornell.jnutella.util.ByteUtils;

@ForMessageType(MessageHeader.F_PING_REPLY)
public class PongEncoder implements MessageBodyEncoder {
  private final GGEPEncoder ggepEncoder;

  @Inject
  public PongEncoder(GGEPEncoder ggepEncoder) {
    this.ggepEncoder = ggepEncoder;
  }

  public void encode(ChannelBuffer buffer, PongBody toEncode) {
    ByteUtils.short2leb((short) toEncode.getPort(), buffer);
    buffer.writeBytes(toEncode.getAddress().getAddress(), 0, 4);
    ByteUtils.int2leb((int) toEncode.getFileCount(), buffer);
    ByteUtils.int2leb((int) toEncode.getFileSizeInKB(), buffer);

    if (toEncode.getGgep() != null) {
      EncoderInput ei = new EncoderInput(toEncode.getGgep(), false);
      ggepEncoder.encode(buffer, ei);
    }
  }

  @Override
  public void encode(ChannelBuffer channel, MessageBody toEncode) {
    Preconditions.checkArgument(toEncode instanceof PongBody, "Not a pong body.");
    encode(channel, (PongBody) toEncode);
  }
  
}
