package edu.cornell.jnutella.messages.decoding;

import org.jboss.netty.buffer.ChannelBuffer;
import org.slf4j.Logger;

import com.google.inject.Inject;

import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.extension.GGEP;
import edu.cornell.jnutella.messages.MessageBodyFactory;
import edu.cornell.jnutella.messages.MessageHeader;
import edu.cornell.jnutella.messages.PingMessage;
import edu.cornell.jnutella.session.gnutella.ForMessageType;

@ForMessageType(MessageHeader.F_PING)
public class PingDecoder implements MessageBodyDecoder<PingMessage> {

  @InjectLogger
  private Logger log;
  private final MessageBodyFactory bodyFactory;
  private final GGEPDecoder ggepDecoder;

  @Inject
  public PingDecoder(MessageBodyFactory bodyFactory, GGEPDecoder ggepDecoder) {
    this.bodyFactory = bodyFactory;
    this.ggepDecoder = ggepDecoder;
  }

  @Override
  public PingMessage decode(ChannelBuffer buffer) {
    if (!buffer.readable()) {
      // we are empty, no ggep
      return bodyFactory.createPingMessage(new GGEP());
    }
    GGEP ggep = ggepDecoder.decode(buffer);
    if (ggep == null) {
      log.error("Could not decode ggep.  Giving back empty ggep.");
      return bodyFactory.createPingMessage(new GGEP());
    }
    return bodyFactory.createPingMessage(ggep);
  }
}
