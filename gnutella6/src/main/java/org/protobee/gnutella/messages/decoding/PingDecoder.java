package org.protobee.gnutella.messages.decoding;

import org.jboss.netty.buffer.ChannelBuffer;
import org.protobee.gnutella.extension.GGEP;
import org.protobee.gnutella.messages.MessageBodyFactory;
import org.protobee.gnutella.messages.MessageHeader;
import org.protobee.gnutella.messages.PingBody;
import org.protobee.gnutella.session.ForMessageType;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;


@ForMessageType(MessageHeader.F_PING)
public class PingDecoder implements MessageBodyDecoder<PingBody> {
  private final MessageBodyFactory bodyFactory;
  private final GGEPDecoder ggepDecoder;

  @Inject
  public PingDecoder(MessageBodyFactory bodyFactory, GGEPDecoder ggepDecoder) {
    this.bodyFactory = bodyFactory;
    this.ggepDecoder = ggepDecoder;
  }

  @Override
  public PingBody decode(ChannelBuffer buffer) throws DecodingException {
    if (!buffer.readable()) {
      return bodyFactory.createPingMessage(null);
    }
    GGEP ggep = ggepDecoder.decode(buffer);
    Preconditions.checkNotNull(ggep, "ggep is null");
    return bodyFactory.createPingMessage(ggep);
  }
}
