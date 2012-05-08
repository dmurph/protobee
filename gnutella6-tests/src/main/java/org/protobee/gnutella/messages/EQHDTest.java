package org.protobee.gnutella.messages;

import static org.junit.Assert.assertEquals;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.protobee.AbstractTest;
import org.protobee.gnutella.messages.decoding.DecodingException;
import org.protobee.gnutella.messages.decoding.EQHDDecoder;
import org.protobee.gnutella.messages.encoding.EQHDEncoder;
import org.protobee.gnutella.messages.encoding.EncodingException;
import org.protobee.gnutella.util.VendorCode;


public class EQHDTest extends AbstractTest {

  @Test
  public void testEmptyMatch() throws EncodingException, DecodingException {
    EQHDBody eqhd = new EQHDBody(new VendorCode('L', 'I', 'M', 'E'), (short) 2, (byte) 0x01, (byte) 0x02);
    ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
    EQHDEncoder encoder = injector.getInstance(EQHDEncoder.class);
    encoder.encode(buffer, eqhd);

    EQHDDecoder decoder = injector.getInstance(EQHDDecoder.class);
    EQHDBody results = decoder.decode(buffer);

    assertEquals(eqhd, results);

  }
  
}
