package edu.cornell.jnutella.messages.encoding;

import org.jboss.netty.buffer.ChannelBuffer;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import edu.cornell.jnutella.messages.MessageBody;
import edu.cornell.jnutella.messages.MessageHeader;
import edu.cornell.jnutella.messages.PingBody;
import edu.cornell.jnutella.messages.encoding.GGEPEncoder.EncoderInput;
import edu.cornell.jnutella.session.gnutella.ForMessageType;

@ForMessageType(MessageHeader.F_PING)
public class PingEncoder implements MessageBodyEncoder<PingBody> {

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
  public void encode(ChannelBuffer channel, MessageBody toEncode) throws EncodingException {
    Preconditions.checkArgument(toEncode instanceof PingBody, "Not a ping body");
    encode(channel, (PingBody) toEncode);
  }
}
