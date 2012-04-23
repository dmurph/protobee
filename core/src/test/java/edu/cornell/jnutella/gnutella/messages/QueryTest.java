package edu.cornell.jnutella.gnutella.messages;

import static org.junit.Assert.assertEquals;

import java.net.UnknownHostException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;

import edu.cornell.jnutella.AbstractTest;
import edu.cornell.jnutella.gnutella.messages.MessageBodyFactory;
import edu.cornell.jnutella.gnutella.messages.QueryBody;
import edu.cornell.jnutella.gnutella.messages.decoding.DecodingException;
import edu.cornell.jnutella.gnutella.messages.decoding.QueryDecoder;
import edu.cornell.jnutella.gnutella.messages.encoding.EncodingException;
import edu.cornell.jnutella.gnutella.messages.encoding.QueryEncoder;

public class QueryTest extends AbstractTest {

  @Test
  public void testQuery() throws DecodingException, EncodingException, UnknownHostException {
    ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();

    MessageBodyFactory factory = injector.getInstance(MessageBodyFactory.class);

    QueryBody query = factory.createQueryMessage( (short) (Integer.MAX_VALUE + 1), "kjhsdfkjs", null, null);

    QueryEncoder encoder = injector.getInstance(QueryEncoder.class);
    encoder.encode(buffer, query);

    QueryDecoder decoder = injector.getInstance(QueryDecoder.class);
    QueryBody results = decoder.decode(buffer);

    assertEquals(query, results);
  }

}
