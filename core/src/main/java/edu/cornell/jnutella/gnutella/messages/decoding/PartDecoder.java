package edu.cornell.jnutella.gnutella.messages.decoding;

import org.jboss.netty.buffer.ChannelBuffer;

public interface PartDecoder<T> {
  T decode(ChannelBuffer buffer) throws DecodingException;
}
