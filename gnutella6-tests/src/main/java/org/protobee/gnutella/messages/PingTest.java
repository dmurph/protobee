package org.protobee.gnutella.messages;

import static org.junit.Assert.assertEquals;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.protobee.gnutella.AbstractGnutellaTest;
import org.protobee.gnutella.extension.GGEP;
import org.protobee.gnutella.messages.decoding.DecodingException;
import org.protobee.gnutella.messages.decoding.PingDecoder;
import org.protobee.gnutella.messages.encoding.PingEncoder;


public class PingTest extends AbstractGnutellaTest {

  @Test
  public void testPing() throws DecodingException{

    MessageBodyFactory factory = injector.getInstance(MessageBodyFactory.class);

    for (GGEP ggep : TestUtils.getGGEPArray()){

      ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();

      PingBody ping = factory.createPingMessage(ggep);

      PingEncoder encoder = injector.getInstance(PingEncoder.class);
      encoder.encode(buffer, ping);

      PingDecoder decoder = injector.getInstance(PingDecoder.class);
      PingBody results = decoder.decode(buffer);

      assertEquals(ping, results);

    }

  }

}
