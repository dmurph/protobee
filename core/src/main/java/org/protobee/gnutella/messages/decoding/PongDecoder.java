package org.protobee.gnutella.messages.decoding;

import java.net.InetSocketAddress;

import org.jboss.netty.buffer.ChannelBuffer;
import org.protobee.extension.GGEP;
import org.protobee.gnutella.messages.MessageBodyFactory;
import org.protobee.gnutella.messages.MessageHeader;
import org.protobee.gnutella.messages.PongBody;
import org.protobee.gnutella.session.ForMessageType;
import org.protobee.util.ByteUtils;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;


@ForMessageType(MessageHeader.F_PING_REPLY)
public class PongDecoder implements MessageBodyDecoder<PongBody> {
  private final MessageBodyFactory bodyFactory;
  private final GGEPDecoder ggepDecoder;
  
  @Inject
  public PongDecoder(MessageBodyFactory bodyFactory, GGEPDecoder ggepDecoder) {
    this.bodyFactory = bodyFactory;
    this.ggepDecoder = ggepDecoder;
  }

  @Override
  public PongBody decode(ChannelBuffer buffer) throws DecodingException {
    Preconditions.checkState(buffer.readableBytes() >= 14);

    int port = ByteUtils.ushort2int(ByteUtils.leb2short(buffer));
    byte[] address = {buffer.readByte(), buffer.readByte(), buffer.readByte(), buffer.readByte()};
    InetSocketAddress socketAddress = ByteUtils.getInetSocketAddress(address, port);
    
    long fileCount = ByteUtils.uint2long(ByteUtils.leb2int(buffer));
    long fileSizeInKB = ByteUtils.uint2long(ByteUtils.leb2int(buffer));

    if (!buffer.readable()) {
      return bodyFactory.createPongMessage(socketAddress, fileCount, fileSizeInKB, null);
    }

    GGEP ggep = ggepDecoder.decode(buffer);
    Preconditions.checkNotNull(ggep, "GGEP is null.");
    return bodyFactory.createPongMessage(socketAddress, fileCount, fileSizeInKB, ggep);
  }
}