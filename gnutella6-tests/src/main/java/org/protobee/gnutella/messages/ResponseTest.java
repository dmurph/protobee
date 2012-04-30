package org.protobee.gnutella.messages;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.protobee.AbstractTest;
import org.protobee.gnutella.extension.HUGEExtension;
import org.protobee.gnutella.messages.decoding.DecodingException;
import org.protobee.gnutella.messages.decoding.ResponseDecoder;
import org.protobee.gnutella.messages.encoding.EncodingException;
import org.protobee.gnutella.messages.encoding.ResponseEncoder;
import org.protobee.gnutella.util.URN;


public class ResponseTest extends AbstractTest {

  @Test
  public void testResponse() throws DecodingException, EncodingException, IOException {
    ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();

    URN[] urns = new URN[5];
    urns[0] = new URN("urn:sha1:ANCKHTQCWBTRNJIV4WNAE52SJUQCZO5C");
    urns[1] = new URN("urn:sha1:BBCKHTQCWBTRNJIV4WNAE52SJUQCZO5F");
    urns[2] = new URN("urn:sha1:CNCKHTQABCTRNJIV4WNAE52SJUQCZO5G");
    urns[3] = new URN("urn:sha1:DNCKHTQCWBTRNJIV4WNAE52SJUQCZO5H");
    urns[4] = new URN("urn:sha1:ENCKHTQCWABCNJIV4WNAE52SJUQCZO5J");

    HUGEExtension huge = new HUGEExtension(urns);
    
    ResponseBody response = new ResponseBody((long) Integer.MAX_VALUE + 1l, 
      (long) Integer.MAX_VALUE + 1l,"FILENAME", huge, null);
    
    ResponseEncoder encoder = injector.getInstance(ResponseEncoder.class);
    encoder.encode(buffer, response);

    ResponseDecoder decoder = injector.getInstance(ResponseDecoder.class);
    ResponseBody results = decoder.decode(buffer);

    assertEquals(response, results);
  }
  
}
