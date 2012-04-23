package edu.cornell.jnutella.gnutella.messages;

import static org.junit.Assert.assertEquals;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;

import edu.cornell.jnutella.AbstractTest;
import edu.cornell.jnutella.gnutella.messages.decoding.DecodingException;
import edu.cornell.jnutella.gnutella.messages.decoding.EQHDDecoder;
import edu.cornell.jnutella.gnutella.messages.encoding.EQHDEncoder;
import edu.cornell.jnutella.gnutella.messages.encoding.EncodingException;
import edu.cornell.jnutella.util.VendorCode;

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
