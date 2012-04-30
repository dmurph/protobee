package org.protobee.gnutella.messages;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.protobee.gnutella.AbstractGnutellaTest;
import org.protobee.gnutella.messages.decoding.DecodingException;
import org.protobee.gnutella.messages.encoding.EncodingException;
import org.protobee.gnutella.routing.InvalidMessageException;
import org.protobee.gnutella.routing.message.PatchBody;
import org.protobee.gnutella.routing.message.ResetBody;
import org.protobee.gnutella.routing.message.RoutingDecoder;
import org.protobee.gnutella.routing.message.RoutingEncoder;


public class RoutingTest extends AbstractGnutellaTest {

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

