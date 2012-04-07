package edu.cornell.jnutella.messages.decoding;

import org.jboss.netty.buffer.ChannelBuffer;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import edu.cornell.jnutella.extension.GGEP;
import edu.cornell.jnutella.messages.MessageBodyFactory;
import edu.cornell.jnutella.messages.MessageHeader;
import edu.cornell.jnutella.messages.PingMessage;
import edu.cornell.jnutella.session.gnutella.ForMessageType;

@ForMessageType(MessageHeader.F_PING)
public class PingDecoder implements MessageBodyDecoder<PingMessage> {
  private final MessageBodyFactory bodyFactory;
  private final GGEPDecoder ggepDecoder;

  @Inject
  public PingDecoder(MessageBodyFactory bodyFactory, GGEPDecoder ggepDecoder) {
    this.bodyFactory = bodyFactory;
    this.ggepDecoder = ggepDecoder;
  }

  @Override
  public PingMessage decode(ChannelBuffer buffer) throws DecodingException {
    if (!buffer.readable()) {
      // we are empty, no ggep
      return bodyFactory.createPingMessage(new GGEP());
    }
    GGEP ggep = ggepDecoder.decode(buffer);
    Preconditions.checkNotNull(ggep, "ggep is null");
    return bodyFactory.createPingMessage(ggep);
  }
}
