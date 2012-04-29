package org.protobee.gnutella.messages;

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.protobee.AbstractTest;
import org.protobee.gnutella.messages.MessageBodyFactory;
import org.protobee.gnutella.messages.PushBody;
import org.protobee.gnutella.messages.decoding.DecodingException;
import org.protobee.gnutella.messages.decoding.PushDecoder;
import org.protobee.gnutella.messages.encoding.EncodingException;
import org.protobee.gnutella.messages.encoding.PushEncoder;
import org.protobee.gnutella.util.GUID;


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
