package edu.cornell.jnutella.messages.decoding;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.extension.GGEP;
import edu.cornell.jnutella.gnutella.session.ForMessageType;
import edu.cornell.jnutella.messages.MessageBodyFactory;
import edu.cornell.jnutella.messages.MessageHeader;
import edu.cornell.jnutella.messages.PongBody;
import edu.cornell.jnutella.util.ByteUtils;

@ForMessageType(MessageHeader.F_PING_REPLY)
public class PongDecoder implements MessageBodyDecoder<PongBody> {
  private final MessageBodyFactory bodyFactory;
  private final GGEPDecoder ggepDecoder;

  @InjectLogger
  private Logger log;
  
  @Inject
  public PongDecoder(MessageBodyFactory bodyFactory, GGEPDecoder ggepDecoder) {
    this.bodyFactory = bodyFactory;
    this.ggepDecoder = ggepDecoder;
  }

  @Override
  public PongBody decode(ChannelBuffer buffer) throws DecodingException {
    Preconditions.checkState(buffer.readableBytes() >= 14);

    int port = ByteUtils.ushort2int(ByteUtils.leb2short(buffer));
    
    int a = ByteUtils.ubyte2int(buffer.readByte());
    int b = ByteUtils.ubyte2int(buffer.readByte());
    int c = ByteUtils.ubyte2int(buffer.readByte());
    int d = ByteUtils.ubyte2int(buffer.readByte());
    String ip = (a + "." + b + "." + c + "." + d);
    
    long fileCount = ByteUtils.uint2long(ByteUtils.leb2int(buffer));
    long fileSizeInKB = ByteUtils.uint2long(ByteUtils.leb2int(buffer));
    InetAddress address;

    try {
      address = InetAddress.getByName(ip);
    } catch (UnknownHostException e) {
      log.error("Host " + ip + " is unknown in Pong.");
      throw new DecodingException("Host " + ip + " is unknown in Pong.");
    }

    if (!buffer.readable()) {
      return bodyFactory.createPongMessage(address, port, fileCount, fileSizeInKB, null);
    }

    GGEP ggep = ggepDecoder.decode(buffer);
    Preconditions.checkNotNull(ggep, "GGEP is null.");
    return bodyFactory.createPongMessage(address, port, fileCount, fileSizeInKB, ggep);
  }
}