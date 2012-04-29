package org.protobee.gnutella.messages.encoding;

import org.jboss.netty.buffer.ChannelBuffer;
import org.protobee.gnutella.messages.MessageBody;
import org.protobee.gnutella.messages.MessageHeader;
import org.protobee.gnutella.messages.PingBody;
import org.protobee.gnutella.messages.encoding.GGEPEncoder.EncoderInput;
import org.protobee.gnutella.session.ForMessageType;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;


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
