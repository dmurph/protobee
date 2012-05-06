package org.protobee.gnutella.messages;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.protobee.gnutella.AbstractGnutellaTest;
import org.protobee.gnutella.extension.GGEP;
import org.protobee.gnutella.messages.decoding.DecodingException;
import org.protobee.gnutella.messages.decoding.PongDecoder;
import org.protobee.gnutella.messages.encoding.EncodingException;
import org.protobee.gnutella.messages.encoding.PongEncoder;


public class PongTest extends AbstractGnutellaTest {

  @Test
  public void testPong1() throws DecodingException, EncodingException, UnknownHostException {
    
    MessageBodyFactory factory = injector.getInstance(MessageBodyFactory.class);

    for (GGEP ggep : TestUtils.getGGEPArray()){

      ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();

      PongBody pong =
          factory.createPongMessage(new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), 
            (int) Short.MAX_VALUE + 1), (long) Integer.MAX_VALUE + 1l, 
            (long) Integer.MAX_VALUE + 2l, ggep);

      PongEncoder encoder = injector.getInstance(PongEncoder.class);
      encoder.encode(buffer, pong);

      PongDecoder decoder = injector.getInstance(PongDecoder.class);
      PongBody results = decoder.decode(buffer);

      assertEquals(pong, results);
    }
  }

}
