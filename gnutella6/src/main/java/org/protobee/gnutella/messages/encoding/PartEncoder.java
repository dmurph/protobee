package org.protobee.gnutella.messages.encoding;

import org.jboss.netty.buffer.ChannelBuffer;

public interface PartEncoder<T> {
  void encode(ChannelBuffer channel, T toEncode) throws EncodingException;
}
