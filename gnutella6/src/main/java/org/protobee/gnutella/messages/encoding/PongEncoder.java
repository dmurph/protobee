package org.protobee.gnutella.messages.encoding;

import org.jboss.netty.buffer.ChannelBuffer;
import org.protobee.gnutella.messages.MessageBody;
import org.protobee.gnutella.messages.MessageHeader;
import org.protobee.gnutella.messages.PongBody;
import org.protobee.gnutella.messages.encoding.GGEPEncoder.EncoderInput;
import org.protobee.gnutella.session.ForMessageType;
import org.protobee.util.ByteUtils;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;


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
