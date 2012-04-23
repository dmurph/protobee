package edu.cornell.jnutella.gnutella.messages;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;

import edu.cornell.jnutella.AbstractTest;
import edu.cornell.jnutella.gnutella.messages.decoding.DecodingException;
import edu.cornell.jnutella.gnutella.messages.decoding.ResponseDecoder;
import edu.cornell.jnutella.gnutella.messages.encoding.EncodingException;
import edu.cornell.jnutella.gnutella.messages.encoding.ResponseEncoder;
import edu.cornell.jnutella.util.URN;

public class ResponseTest extends AbstractTest {

  @Test
  public void testResponse() throws DecodingException, EncodingException, IOException {
    ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();

    ResponseBody response = new ResponseBody((long) Integer.MAX_VALUE + 1l, 
      (long) Integer.MAX_VALUE + 1l,"FILENAME", 
      new URN("urn:sha1:YNCKHTQCWBTRNJIV4WNAE52SJUQCZO5C"), null);
    // urn -- new URN("urn:sha1:YNCKHTQCWBTRNJIV4WNAE52SJUQCZO5C")
    ResponseEncoder encoder = injector.getInstance(ResponseEncoder.class);
    encoder.encode(buffer, response);

    ResponseDecoder decoder = injector.getInstance(ResponseDecoder.class);
    ResponseBody results = decoder.decode(buffer);

    assertEquals(response, results);
  }
  
}
