package org.protobee.examples.broadcast;

import java.util.Random;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.local.LocalAddress;
import org.junit.Test;
import org.protobee.JnutellaServantBootstrapper;
import org.protobee.ProtobeeGuiceModule;
import org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage;
import org.protobee.examples.protos.Common.Header;
import org.protobee.netty.LocalNettyTester;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.protobuf.ByteString;

public class BroadcastProtocolTests extends AbstractBroadcastTest {

  @Test
  public void testBroadcast() throws Exception {
    Injector inj = Guice.createInjector(new ProtobeeGuiceModule(), new BroadcastGuiceModule());

    JnutellaServantBootstrapper bootstrap = inj.getInstance(JnutellaServantBootstrapper.class);
    bootstrap.startup();

    LocalNettyTester tester1 = new LocalNettyTester();
    tester1.connect(new LocalAddress("broadcast-example"), new LocalAddress("test1"));
    basicHandshake(tester1);

    LocalNettyTester tester2 = new LocalNettyTester();
    tester2.connect(new LocalAddress("broadcast-example"), new LocalAddress("test2"));
    basicHandshake(tester2);

    Random random = new Random();
    byte[] messageId = new byte[16];
    String messageStr = "SUCCESS";
    random.nextBytes(messageId);

    BroadcastMessage message =
        BroadcastMessage
            .newBuilder()
            .setHeader(
                Header.newBuilder().setHops(0).setId(ByteString.copyFrom(messageId)).setTtl(2))
            .setMessage(messageStr).build();
    tester1.writeAndWait(ChannelBuffers.wrappedBuffer(message.toByteArray()), 1000);

    BroadcastMessage received =
        BroadcastMessage
            .newBuilder()
            .setHeader(
                Header.newBuilder().setHops(1).setId(ByteString.copyFrom(messageId)).setTtl(1))
            .setMessage(messageStr).build();

    tester2.verifyReceived(ChannelBuffers.wrappedBuffer(received.toByteArray()));
    tester1.verifyNothingReceived();

    tester1.verifyNotClosed();
    tester2.verifyNotClosed();
    bootstrap.shutdown(1000);
  }
}