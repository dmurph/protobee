package edu.cornell.jnutella.messages;

import static org.junit.Assert.assertEquals;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;

import edu.cornell.jnutella.AbstractTest;
import edu.cornell.jnutella.extension.GGEP;
import edu.cornell.jnutella.messages.decoding.DecodingException;
import edu.cornell.jnutella.messages.decoding.GGEPDecoder;
import edu.cornell.jnutella.messages.decoding.ResponseDecoder;
import edu.cornell.jnutella.messages.encoding.EncodingException;
import edu.cornell.jnutella.messages.encoding.GGEPEncoder;
import edu.cornell.jnutella.messages.encoding.GGEPEncoder.EncoderInput;
import edu.cornell.jnutella.messages.encoding.ResponseEncoder;
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
  public void testResponse() throws DecodingException, EncodingException {
    ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();

    ResponseBody response = new ResponseBody((long) Integer.MAX_VALUE + 1l, 
      (long) Integer.MAX_VALUE + 1l,"FILENAME", 
      new URN("urn:sha1:YNCKHTQCWBTRNJIV4WNAE52SJUQCZO5C"), null);
    ResponseEncoder encoder = injector.getInstance(ResponseEncoder.class);
    encoder.encode(buffer, response);

    ResponseDecoder decoder = injector.getInstance(ResponseDecoder.class);
    ResponseBody results = decoder.decode(buffer);
    
    assertEquals(results, results);
  }
  
}