package edu.cornell.jnutella.gnutella.messages.encoding;

import org.jboss.netty.buffer.ChannelBuffer;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import edu.cornell.jnutella.gnutella.messages.MessageBody;
import edu.cornell.jnutella.gnutella.messages.MessageHeader;
import edu.cornell.jnutella.gnutella.messages.PingBody;
import edu.cornell.jnutella.gnutella.messages.encoding.GGEPEncoder.EncoderInput;
import edu.cornell.jnutella.gnutella.session.ForMessageType;

@ForMessageType(MessageHeader.F_PING)
public class PingEncoder implements MessageBodyEncoder {

  private final GGEPEncoder ggepEncoder;

  @Inject
  public PingEncoder(GGEPEncoder ggepEncoder) {
    this.ggepEncoder = ggepEncoder;
  }

  public void encode(ChannelBuffer channel, PingBody toEncode){
    if(toEncode.getGgep() != null) {
      EncoderInput ei = new EncoderInput(toEncode.getGgep(), false);      
      ggepEncoder.encode(channel, ei);
    }
  }

  @Override
  public void encode(ChannelBuffer channel, MessageBody toEncode) {
    Preconditions.checkArgument(toEncode instanceof PingBody, "Not a ping body");
    encode(channel, (PingBody) toEncode);
  }
}
