package edu.cornell.jnutella.gnutella.messages;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;

import edu.cornell.jnutella.AbstractTest;
import edu.cornell.jnutella.extension.GGEP;
import edu.cornell.jnutella.gnutella.messages.decoding.DecodingException;
import edu.cornell.jnutella.gnutella.messages.decoding.GGEPDecoder;
import edu.cornell.jnutella.gnutella.messages.decoding.ResponseDecoder;
import edu.cornell.jnutella.gnutella.messages.encoding.EncodingException;
import edu.cornell.jnutella.gnutella.messages.encoding.GGEPEncoder;
import edu.cornell.jnutella.gnutella.messages.encoding.GGEPEncoder.EncoderInput;
import edu.cornell.jnutella.gnutella.messages.encoding.ResponseEncoder;
import edu.cornell.jnutella.util.URN;

public class ResponseTest extends AbstractTest {

  public void testEmptyMatch() throws EncodingException, DecodingException {
    GGEP ggep = new GGEP();
    ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
    GGEPEncoder encoder = injector.getInstance(GGEPEncoder.class);
    encoder.encode(buffer, new EncoderInput(ggep, false));

    GGEPDecoder decoder = injector.getInstance(GGEPDecoder.class);
    GGEP results = decoder.decode(buffer);

    assertEquals(ggep, results);
    assertEquals(0, results.getNumKeys());
  }


  @Test
  public void testResponse() throws DecodingException, EncodingException, IOException {
    ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();

    ResponseBody response = new ResponseBody((long) Integer.MAX_VALUE + 1l, 
      (long) Integer.MAX_VALUE + 1l,"FILENAME", 
      URN.createSHA1Urn("urn:sha1:YNCKHTQCWBTRNJIV4WNAE52SJUQCZO5C"), null);
    // urn -- new URN("urn:sha1:YNCKHTQCWBTRNJIV4WNAE52SJUQCZO5C")
    ResponseEncoder encoder = injector.getInstance(ResponseEncoder.class);
    encoder.encode(buffer, response);

    ResponseDecoder decoder = injector.getInstance(ResponseDecoder.class);
    ResponseBody results = decoder.decode(buffer);

    assertEquals(results, results);
  }

}
