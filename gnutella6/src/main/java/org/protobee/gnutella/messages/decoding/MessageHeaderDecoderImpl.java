package org.protobee.gnutella.messages.decoding;

import org.jboss.netty.buffer.ChannelBuffer;
import org.protobee.gnutella.messages.MessageHeader;


public class MessageHeaderDecoderImpl implements MessageHeaderDecoder {

  @Override
  public MessageHeader decode(ChannelBuffer buffer) {
    if (buffer.readableBytes() < 23) {
      return null;
    }

    byte[] guid = buffer.readBytes(16).array();
    byte type = buffer.readByte();
    byte ttl = buffer.readByte();
    byte hops = buffer.readByte();
    int length = buffer.readInt();
    return new MessageHeader(guid, type, ttl, hops, length);
  }
}
