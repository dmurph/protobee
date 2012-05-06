package org.protobee.examples.broadcast;

import org.jboss.netty.buffer.ChannelBuffers;
import org.protobee.AbstractTest;
import org.protobee.netty.LocalNettyTester;

public class AbstractBroadcastTest extends AbstractTest {
  private static String handshake0 = "SAY / BROADCAST/0.1\r\n\r\n";
  private static String handshake1_rec = "BROADCAST/0.1 200 OK\r\n\r\n";
  private static String handshake2 = "BROADCAST/0.1 200 OK\r\n\r\n";

  public static void basicHandshake(LocalNettyTester tester) {
    tester.writeAndWait(ChannelBuffers.wrappedBuffer(handshake0.getBytes()), 1000);
    tester.verifyReceived(ChannelBuffers.wrappedBuffer(handshake1_rec.getBytes()));
    tester.writeAndWait(ChannelBuffers.wrappedBuffer(handshake2.getBytes()), 1000);
  }
}
