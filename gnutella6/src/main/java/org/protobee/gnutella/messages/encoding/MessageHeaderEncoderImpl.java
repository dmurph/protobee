package org.protobee.gnutella.messages.encoding;

import org.jboss.netty.buffer.ChannelBuffer;
import org.protobee.gnutella.messages.MessageHeader;
import org.protobee.util.ByteUtils;


public class MessageHeaderEncoderImpl implements MessageHeaderEncoder {

  @Override
  public void encode(ChannelBuffer channel, MessageHeader toEncode) {
    channel.writeBytes(toEncode.getGuid(), 0, 16);
    channel.writeByte(toEncode.getPayloadType());
    channel.writeByte(toEncode.getTtl());
    channel.writeByte(toEncode.getHops());
    ByteUtils.int2leb(toEncode.getPayloadLength(), channel);
  }

}
