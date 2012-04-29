package edu.cornell.jnutella.gnutella.messages;

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;

import edu.cornell.jnutella.AbstractTest;
import edu.cornell.jnutella.gnutella.messages.decoding.DecodingException;
import edu.cornell.jnutella.gnutella.messages.decoding.PushDecoder;
import edu.cornell.jnutella.gnutella.messages.encoding.EncodingException;
import edu.cornell.jnutella.gnutella.messages.encoding.PushEncoder;
import edu.cornell.jnutella.util.GUID;

public class PushTest extends AbstractTest {

  @Test
  public void testPush() throws DecodingException, EncodingException, UnknownHostException, UnsupportedEncodingException {
    
    ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
    MessageBodyFactory factory = injector.getInstance(MessageBodyFactory.class);
    GUID guid = new GUID();
    
    PushBody push = factory.createPushMessage(guid.getBytes(), 
      (long) Integer.MAX_VALUE + 1l, new InetSocketAddress(InetAddress.getLocalHost(), 
      (int) Short.MAX_VALUE + 1), null);

    PushEncoder encoder = injector.getInstance(PushEncoder.class);
    encoder.encode(buffer, push);

    PushDecoder decoder = injector.getInstance(PushDecoder.class);
    PushBody results = decoder.decode(buffer);

    assertEquals(push, results);
  }

}
