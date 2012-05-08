package org.protobee.gnutella.messages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.protobee.AbstractTest;
import org.protobee.gnutella.extension.GGEP;
import org.protobee.gnutella.messages.decoding.DecodingException;
import org.protobee.gnutella.messages.decoding.GGEPDecoder;
import org.protobee.gnutella.messages.encoding.EncodingException;
import org.protobee.gnutella.messages.encoding.GGEPEncoder;
import org.protobee.gnutella.messages.encoding.GGEPEncoder.EncoderInput;


public class GGEPTest extends AbstractTest {


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
  public void testBasicValues() throws EncodingException, DecodingException {
    GGEP ggep = new GGEP();

    ggep.put("key");
    ggep.put("byte=1", 1);
    ggep.put("int=2", 2);
    ggep.put("long=432", 432);
    ggep.put("string=hello", "hello");

    ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();

    GGEPEncoder encoder = injector.getInstance(GGEPEncoder.class);

    encoder.encode(buffer, new EncoderInput(ggep, false));

    buffer.resetReaderIndex();

    GGEPDecoder decoder = injector.getInstance(GGEPDecoder.class);

    GGEP results = decoder.decode(buffer);

    assertEquals(ggep, results);

    assertTrue(results.hasKey("key"));
    assertEquals(ggep.getBytes("byte=1")[0], 1);
    assertEquals(ggep.getInt("int=2"), 2);
    assertEquals(ggep.getLong("long=432"), 432);
    assertEquals(ggep.getString("string=hello"), "hello");
  }

  @Test
  public void testCobs() throws EncodingException {
    GGEP ggep = new GGEP();
    ggep.put("null value", 0);
    ggep.put("str w/ nulls", "hello \0 \0 there");

    ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
    GGEPEncoder encoder = injector.getInstance(GGEPEncoder.class);
    encoder.encode(buffer, new EncoderInput(ggep, true));

    byte[] message = new byte[buffer.readableBytes()];
    buffer.readBytes(message);
    for (int i = 0; i < message.length; i++) {
      assertTrue(0 != message[i]);
    }
  }
}
