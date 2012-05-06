package org.protobee.gnutella.messages;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.protobee.AbstractTest;
import org.protobee.gnutella.extension.HUGEExtension;
import org.protobee.gnutella.messages.decoding.DecodingException;
import org.protobee.gnutella.messages.decoding.HUGEDecoder;
import org.protobee.gnutella.messages.encoding.EncodingException;
import org.protobee.gnutella.messages.encoding.HUGEEncoder;


public class HUGETest extends AbstractTest {

  @Test
  public void testHUGE() throws EncodingException, DecodingException, IOException {

    HUGEExtension[] hugeArr = TestUtils.getHUGEArray();
    
    for (int i = 1; i < hugeArr.length; i++){

      ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();

      HUGEEncoder encoder = injector.getInstance(HUGEEncoder.class);
      encoder.encode(buffer, hugeArr[i]);

      HUGEDecoder decoder = injector.getInstance(HUGEDecoder.class);
      HUGEExtension results = decoder.decode(buffer);

      assertEquals(hugeArr[i], results);
    
    }
  }
}
