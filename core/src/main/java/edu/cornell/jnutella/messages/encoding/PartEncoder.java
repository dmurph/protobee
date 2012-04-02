package edu.cornell.jnutella.messages.encoding;

import org.jboss.netty.buffer.ChannelBuffer;

public interface PartEncoder<T> {
  Class<T> getEncodingClass();

  void encode(ChannelBuffer channel, T toEncode);
}
