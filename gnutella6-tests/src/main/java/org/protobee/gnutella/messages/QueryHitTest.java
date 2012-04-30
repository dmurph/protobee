package org.protobee.gnutella.messages;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.protobee.gnutella.AbstractGnutellaTest;
import org.protobee.gnutella.messages.decoding.DecodingException;
import org.protobee.gnutella.messages.decoding.QueryHitDecoder;
import org.protobee.gnutella.messages.encoding.EncodingException;
import org.protobee.gnutella.messages.encoding.QueryHitEncoder;
import org.protobee.gnutella.util.GUID;
import org.protobee.gnutella.util.VendorCode;


public class QueryHitTest extends AbstractGnutellaTest {

  @Test
  public void testQueryHit() throws DecodingException, EncodingException, UnknownHostException {
    ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();

    MessageBodyFactory factory = injector.getInstance(MessageBodyFactory.class);

    byte[] privateArea1 = {(byte) 0x03, (byte) 0x04, (byte) 0x05};
    byte[] xmlBytes = {(byte) 0x06, (byte) 0x07, (byte) 0x08};
    byte[] privateArea2 = {(byte) 0x09, (byte) 0x10, (byte) 0x11};
    GUID guid = new GUID();

    QueryHitBody queryHit = factory.createQueryHitMessage(new InetSocketAddress( InetAddress.getLocalHost(),
      (short) (Integer.MAX_VALUE + 1)), (long) Integer.MAX_VALUE + 1l, null, new VendorCode('L', 'I', 'M', 'E'), 
      (byte) 0x01, (byte) 0x02, privateArea1, null, xmlBytes, privateArea2, guid.getBytes());

    QueryHitEncoder encoder = injector.getInstance(QueryHitEncoder.class);
    encoder.encode(buffer, queryHit);

    QueryHitDecoder decoder = injector.getInstance(QueryHitDecoder.class);
    QueryHitBody results = decoder.decode(buffer);

    assertEquals(queryHit, results);
  }

}
