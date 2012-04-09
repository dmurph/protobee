package edu.cornell.jnutella.messages.decoding;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.extension.GGEP;
import edu.cornell.jnutella.messages.MessageBodyFactory;
import edu.cornell.jnutella.messages.MessageHeader;
import edu.cornell.jnutella.messages.PongBody;
import edu.cornell.jnutella.session.gnutella.ForMessageType;
import edu.cornell.jnutella.util.ByteUtils;
import edu.cornell.jnutella.util.IOUtils;

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
    if (!buffer.readable()) {
      log.error("Pong buffer is not readable.");
      throw new DecodingException("Pong buffer is not readable.");
    }

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
      log.error("Host " + ip + " is unknown.");
      throw new DecodingException("Host " + ip + " is unknown.");
    }

    if (!buffer.readable()) {
      return bodyFactory.createPongMessage(address, port, fileCount, fileSizeInKB, null);
    }

    GGEP ggep = ggepDecoder.decode(buffer);
    Preconditions.checkNotNull(ggep, "GGEP is null.");
    return bodyFactory.createPongMessage(address, port, fileCount, fileSizeInKB, ggep);
  }
}

// pong structure
// 0-1 Port number. The port number on which the responding
// host can accept incoming connections.
// 2-5 IP Address. The IP address of the responding host.
// Note: This field is in big-endian format.
// 6-9 Number of shared files. The number of files that the
// servent with the given IP address and port is sharing
// on the network.
// 10-13 Number of kilobytes shared. The number of kilobytes
// of data that the servent with the given IP address and
// port is sharing on the network.
// 14- OPTIONAL GGEP extension block. (see Section 2.3)
