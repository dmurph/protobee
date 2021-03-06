package org.protobee.gnutella.messages;

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.protobee.gnutella.AbstractGnutellaTest;
import org.protobee.gnutella.extension.GGEP;
import org.protobee.gnutella.messages.decoding.DecodingException;
import org.protobee.gnutella.messages.decoding.PushDecoder;
import org.protobee.gnutella.messages.encoding.EncodingException;
import org.protobee.gnutella.messages.encoding.PushEncoder;
import org.protobee.gnutella.util.GUID;


public class PushTest extends AbstractGnutellaTest {

  @Test
  public void testPush() throws DecodingException, EncodingException, UnknownHostException, UnsupportedEncodingException {

    MessageBodyFactory factory = injector.getInstance(MessageBodyFactory.class);
    GUID guid = new GUID();

    for (GGEP ggep : TestUtils.getGGEPArray()){

      ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
      
      PushBody push = factory.createPushMessage(guid.getBytes(), 
        (long) Integer.MAX_VALUE + 1l, new InetSocketAddress(InetAddress.getLocalHost(), 
          (int) Short.MAX_VALUE + 1), ggep);

      PushEncoder encoder = injector.getInstance(PushEncoder.class);
      encoder.encode(buffer, push);

      PushDecoder decoder = injector.getInstance(PushDecoder.class);
      PushBody results = decoder.decode(buffer);

      assertEquals(push, results);
    }
  }

}
