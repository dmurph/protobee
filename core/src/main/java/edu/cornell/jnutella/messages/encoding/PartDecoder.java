package edu.cornell.jnutella.messages.encoding;

import org.jboss.netty.buffer.ChannelBuffer;

public interface PartDecoder<T> {
  T decode(ChannelBuffer buffer, int startOffset);
}
