package edu.cornell.jnutella.messages.encoding;

import org.jboss.netty.buffer.ChannelBuffer;

import edu.cornell.jnutella.messages.MessageHeader;
import edu.cornell.jnutella.util.ByteUtils;

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
