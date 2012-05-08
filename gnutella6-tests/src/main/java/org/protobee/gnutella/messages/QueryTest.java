package org.protobee.gnutella.messages;

import static org.junit.Assert.assertEquals;

import java.net.UnknownHostException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.protobee.gnutella.AbstractGnutellaTest;
import org.protobee.gnutella.extension.GGEP;
import org.protobee.gnutella.extension.HUGEExtension;
import org.protobee.gnutella.messages.decoding.DecodingException;
import org.protobee.gnutella.messages.decoding.QueryDecoder;
import org.protobee.gnutella.messages.encoding.EncodingException;
import org.protobee.gnutella.messages.encoding.QueryEncoder;


public class QueryTest extends AbstractGnutellaTest {

  @Test
  public void testQuery() throws DecodingException, EncodingException, UnknownHostException {

    for (HUGEExtension huge: TestUtils.getHUGEArray()){
      for (GGEP ggep: TestUtils.getGGEPArray()){
        System.out.println();
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();

        MessageBodyFactory factory = injector.getInstance(MessageBodyFactory.class);

        QueryBody query1 = factory.createQueryMessage( (short) (Integer.MAX_VALUE + 1), "kjhsdfkjs", huge, ggep);

        QueryEncoder encoder = injector.getInstance(QueryEncoder.class);
        encoder.encode(buffer, query1);

        QueryDecoder decoder = injector.getInstance(QueryDecoder.class);
        QueryBody results = decoder.decode(buffer);

        assertEquals(query1, results);
        
        assertEquals(query1, results);

      }
    }
  }

}
