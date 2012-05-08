package org.protobee.examples.emotions;

import org.jboss.netty.buffer.ChannelBuffers;
import org.protobee.AbstractTest;
import org.protobee.netty.LocalNettyTester;

public class AbstractEmotionsTest extends AbstractTest {
  public static final String handshake0 = "SAY / EMOTION/0.1\r\n\r\n";
  public static final String handshake1 = "EMOTION/0.1 200 OK\r\n\r\n";
  public static final String handshake2 = "EMOTION/0.1 200 OK\r\n\r\n";

  public static void basicHandshake(LocalNettyTester tester) {
    basicHandshake(tester, handshake0, handshake1, handshake2);
  }

  public static void basicHandshake(LocalNettyTester tester, String handshake0,
                                    String handshake1_rec, String handshake2) {
    tester.writeAndWait(ChannelBuffers.wrappedBuffer(handshake0.getBytes()), 1000);
    tester.verifyReceived(ChannelBuffers.wrappedBuffer(handshake1_rec.getBytes()));
    tester.writeAndWait(ChannelBuffers.wrappedBuffer(handshake2.getBytes()), 1000);
  }
}



