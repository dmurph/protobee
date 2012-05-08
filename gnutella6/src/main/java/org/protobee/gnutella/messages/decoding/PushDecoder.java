package org.protobee.gnutella.messages.decoding;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.protobee.gnutella.extension.GGEP;
import org.protobee.gnutella.messages.MessageBodyFactory;
import org.protobee.gnutella.messages.MessageHeader;
import org.protobee.gnutella.messages.PushBody;
import org.protobee.gnutella.session.ForMessageType;
import org.protobee.util.ByteUtils;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

@ForMessageType(MessageHeader.F_PUSH)
public class PushDecoder implements MessageBodyDecoder<PushBody> {
  private final MessageBodyFactory bodyFactory;
  private final GGEPDecoder ggepDecoder;

  @Inject
  public PushDecoder(MessageBodyFactory bodyFactory, GGEPDecoder ggepDecoder) {
    this.bodyFactory = bodyFactory;
    this.ggepDecoder = ggepDecoder;
  }

  @Override
  public PushBody decode(ChannelBuffer buffer) throws DecodingException {
    Preconditions.checkState(buffer.readableBytes() >= 26);

    byte[] servantID = new byte[16];
    buffer.readBytes(servantID);

    long index = ByteUtils.uint2long(ByteUtils.leb2int(buffer));

    byte[] address = {buffer.readByte(), buffer.readByte(), buffer.readByte(), buffer.readByte()};
    int port = ByteUtils.ushort2int(ByteUtils.leb2short(buffer));
    InetSocketAddress socketAddress;
    try {
      socketAddress = new InetSocketAddress(InetAddress.getByAddress(address), port);
    } catch (UnknownHostException e) {
      throw new DecodingException(e);
    }
    if (!buffer.readable()) {
      return bodyFactory.createPushMessage(servantID, index, socketAddress, null);
    }

    GGEP ggep = ggepDecoder.decode(buffer);
    Preconditions.checkNotNull(ggep, "GGEP is null.");
    return bodyFactory.createPushMessage(servantID, index, socketAddress, ggep);
  }
}
