package org.protobee.gnutella.messages;

import static org.junit.Assert.assertEquals;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.protobee.AbstractTest;
import org.protobee.gnutella.messages.MessageBodyFactory;
import org.protobee.gnutella.messages.PingBody;
import org.protobee.gnutella.messages.decoding.DecodingException;
import org.protobee.gnutella.messages.decoding.PingDecoder;
import org.protobee.gnutella.messages.encoding.PingEncoder;


public class PingTest extends AbstractTest {

  @Test
  public void testPing() throws DecodingException{
    ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
   
    MessageBodyFactory factory = injector.getInstance(MessageBodyFactory.class);
    PingBody ping = factory.createPingMessage(null);
    
    PingEncoder encoder = injector.getInstance(PingEncoder.class);
    encoder.encode(buffer, ping);
    
    PingDecoder decoder = injector.getInstance(PingDecoder.class);
    PingBody results = decoder.decode(buffer);
    
    assertEquals(ping, results);
    
  }
  
}
