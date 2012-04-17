package edu.cornell.jnutella.gnutella.messages;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;

import edu.cornell.jnutella.AbstractTest;
import edu.cornell.jnutella.gnutella.messages.decoding.DecodingException;
import edu.cornell.jnutella.gnutella.messages.decoding.PongDecoder;
import edu.cornell.jnutella.gnutella.messages.encoding.EncodingException;
import edu.cornell.jnutella.gnutella.messages.encoding.PongEncoder;
import edu.cornell.jnutella.util.ByteUtils;
import edu.cornell.jnutella.util.JnutellaSocketAddress;

public class PongTest extends AbstractTest {

  @Test
  public void testPong() throws DecodingException, EncodingException, UnknownHostException {
    ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();

    MessageBodyFactory factory = injector.getInstance(MessageBodyFactory.class);

    PongBody pong =
        factory.createPongMessage(new JnutellaSocketAddress(InetAddress.getLocalHost(), (int) Short.MAX_VALUE + 1),
            (long) Integer.MAX_VALUE + 1l, (long) Integer.MAX_VALUE + 2l, null);

    PongEncoder encoder = injector.getInstance(PongEncoder.class);
    encoder.encode(buffer, pong);

    assertEquals(14, buffer.readableBytes());

    PongDecoder decoder = injector.getInstance(PongDecoder.class);
    PongBody results = decoder.decode(buffer);

    assertEquals(pong, results);
  }

  @Test
  public void miscByteTests() {
    byte[] bytes = new byte[4];
    ByteUtils.int2leb(Integer.MIN_VALUE, bytes, 0);
    int integer = ByteUtils.leb2int(bytes, 0);
    assertEquals(Integer.MIN_VALUE, integer);

    long number = (long) Integer.MAX_VALUE + 1;
    bytes = new byte[4];
    ByteUtils.int2leb((int) number, bytes, 0);
    integer = ByteUtils.leb2int(bytes, 0);
    assertEquals(number, ByteUtils.uint2long(integer));
  }

}