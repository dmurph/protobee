package edu.cornell.jnutella.gnutella.messages;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;

import edu.cornell.jnutella.AbstractTest;
import edu.cornell.jnutella.extension.GGEP;
import edu.cornell.jnutella.extension.HUGEExtension;
import edu.cornell.jnutella.gnutella.messages.decoding.DecodingException;
import edu.cornell.jnutella.gnutella.messages.decoding.HUGEDecoder;
import edu.cornell.jnutella.gnutella.messages.encoding.EncodingException;
import edu.cornell.jnutella.gnutella.messages.encoding.HUGEEncoder;
import edu.cornell.jnutella.util.URN;

public class HUGETest extends AbstractTest {

  @Test
  public void testHUGE() throws EncodingException, DecodingException, IOException {
    GGEP ggep = null;
    URN[] urns = new URN[5];
    urns[0] = new URN("urn:sha1:ANCKHTQCWBTRNJIV4WNAE52SJUQCZO5C");
    urns[1] = new URN("urn:sha1:BBCKHTQCWBTRNJIV4WNAE52SJUQCZO5F");
    urns[2] = new URN("urn:sha1:CNCKHTQABCTRNJIV4WNAE52SJUQCZO5G");
    urns[3] = new URN("urn:sha1:DNCKHTQCWBTRNJIV4WNAE52SJUQCZO5H");
    urns[4] = new URN("urn:sha1:ENCKHTQCWABCNJIV4WNAE52SJUQCZO5J");

    HUGEExtension huge = new HUGEExtension(urns, ggep);

    ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();

    HUGEEncoder encoder = injector.getInstance(HUGEEncoder.class);
    encoder.encode(buffer, huge);

    HUGEDecoder decoder = injector.getInstance(HUGEDecoder.class);
    HUGEExtension results = decoder.decode(buffer);

    assertEquals(huge, results);
  }

}
