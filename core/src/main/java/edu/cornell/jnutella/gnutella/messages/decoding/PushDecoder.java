package edu.cornell.jnutella.gnutella.messages.decoding;

import java.net.InetSocketAddress;

import org.jboss.netty.buffer.ChannelBuffer;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import edu.cornell.jnutella.extension.GGEP;
import edu.cornell.jnutella.gnutella.messages.MessageBodyFactory;
import edu.cornell.jnutella.gnutella.messages.MessageHeader;
import edu.cornell.jnutella.gnutella.messages.PushBody;
import edu.cornell.jnutella.gnutella.session.ForMessageType;
import edu.cornell.jnutella.util.ByteUtils;
import edu.cornell.jnutella.util.GUID;
import edu.cornell.jnutella.util.HexConverter;

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
    for (int i = 0; i < 16; i++){
      servantID[i] = buffer.readByte();
    }
    
    long index = ByteUtils.uint2long(ByteUtils.leb2int(buffer));
    
    byte[] address = {buffer.readByte(), buffer.readByte(), buffer.readByte(), buffer.readByte()};
    int port = ByteUtils.ushort2int(ByteUtils.leb2short(buffer));
    InetSocketAddress socketAddress = ByteUtils.getInetSocketAddress(address, port);
    
    if (!buffer.readable()) {
      return bodyFactory.createPushMessage(servantID, index, socketAddress, null);
    }

    GGEP ggep = ggepDecoder.decode(buffer);
    Preconditions.checkNotNull(ggep, "GGEP is null.");
    return bodyFactory.createPushMessage(servantID, index, socketAddress, ggep);
  }
}