package org.protobee.gnutella.messages;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.protobee.AbstractTest;
import org.protobee.gnutella.extension.GGEP;
import org.protobee.gnutella.extension.HUGEExtension;
import org.protobee.gnutella.messages.decoding.DecodingException;
import org.protobee.gnutella.messages.decoding.ResponseDecoder;
import org.protobee.gnutella.messages.encoding.EncodingException;
import org.protobee.gnutella.messages.encoding.ResponseEncoder;


public class ResponseTest extends AbstractTest {

  @Test
  public void testResponse() throws DecodingException, EncodingException, IOException {

    for (HUGEExtension huge: TestUtils.getHUGEArray()){
      for (GGEP ggep: TestUtils.getGGEPArray()){

        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();

        ResponseBody response = new ResponseBody((long) Integer.MAX_VALUE + 1l, 
          (long) Integer.MAX_VALUE + 1l,"FILENAME", huge, ggep);

        ResponseEncoder encoder = injector.getInstance(ResponseEncoder.class);
        encoder.encode(buffer, response);

        ResponseDecoder decoder = injector.getInstance(ResponseDecoder.class);
        ResponseBody results = decoder.decode(buffer);

        assertEquals(response, results);
        
      }
    }
  }
}
