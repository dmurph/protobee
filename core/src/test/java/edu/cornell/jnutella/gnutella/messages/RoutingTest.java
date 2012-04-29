package edu.cornell.jnutella.gnutella.messages;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;

import edu.cornell.jnutella.AbstractTest;
import edu.cornell.jnutella.gnutella.messages.decoding.DecodingException;
import edu.cornell.jnutella.gnutella.messages.encoding.EncodingException;
import edu.cornell.jnutella.gnutella.routing.InvalidMessageException;
import edu.cornell.jnutella.gnutella.routing.message.PatchBody;
import edu.cornell.jnutella.gnutella.routing.message.ResetBody;
import edu.cornell.jnutella.gnutella.routing.message.RoutingDecoder;
import edu.cornell.jnutella.gnutella.routing.message.RoutingEncoder;

public class RoutingTest extends AbstractTest {

  @Test
  public void testPatch() throws DecodingException, EncodingException, IOException, InvalidMessageException {
    ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();

    byte[] data = {(byte) 0x6, (byte) 0x7, (byte) 0x8};
    PatchBody patch = new PatchBody((byte) 0x1, (byte) 0x2, (byte) 0x3, (byte) 0x4, data);
    
    RoutingEncoder encoder = injector.getInstance(RoutingEncoder.class);
    encoder.encode(buffer, patch);

    RoutingDecoder decoder = injector.getInstance(RoutingDecoder.class);
    PatchBody results = (PatchBody) decoder.decode(buffer);

    assertEquals(patch, results);
  }
  
  @Test
  public void testReset() throws DecodingException, EncodingException, IOException, InvalidMessageException {
    ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
    ResetBody reset = new ResetBody( (long) Integer.MAX_VALUE + 1l, (byte) 0x1);
    
    RoutingEncoder encoder = injector.getInstance(RoutingEncoder.class);
    encoder.encode(buffer, reset);

    RoutingDecoder decoder = injector.getInstance(RoutingDecoder.class);
    ResetBody results = (ResetBody) decoder.decode(buffer);

    assertEquals(reset, results);
  }
  
}

