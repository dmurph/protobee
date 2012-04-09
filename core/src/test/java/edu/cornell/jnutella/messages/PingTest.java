package edu.cornell.jnutella.messages;

import static org.junit.Assert.assertEquals;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;

import edu.cornell.jnutella.AbstractTest;
import edu.cornell.jnutella.messages.decoding.DecodingException;
import edu.cornell.jnutella.messages.decoding.PingDecoder;
import edu.cornell.jnutella.messages.encoding.PingEncoder;

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
