package org.protobee.examples.broadcast;

import java.util.Random;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.local.LocalAddress;
import org.junit.Test;
import org.protobee.ProtobeeGuiceModule;
import org.protobee.ProtobeeServantBootstrapper;
import org.protobee.examples.emotion.EmotionGuiceModule;
import org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage;
import org.protobee.examples.protos.Common.Header;
import org.protobee.netty.LocalNettyTester;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.google.protobuf.ByteString;

public class BroadcastProtocolTests extends AbstractBroadcastTest {

  private static final String TEST_ADDRESS_2 = "32.123.5.3:234";
  private static final String TEST_ADDRESS_1 = "5.23.2.1:66";

  @Test
  public void testBroadcast() throws Exception {
    Injector inj =
        Guice.createInjector(Modules
            .override(new ProtobeeGuiceModule(), new BroadcastGuiceModule(), new EmotionGuiceModule()).with(
                new LocalChannelsModule()));

    ProtobeeServantBootstrapper bootstrap = inj.getInstance(ProtobeeServantBootstrapper.class);
    bootstrap.startup();

    LocalNettyTester tester1 = createLocalNettyTester();

    LocalAddress localAddress = new LocalAddress(LocalChannelsModule.LOCAL_ADDRESS);
    tester1.connect(localAddress, new LocalAddress(TEST_ADDRESS_1));
    basicHandshake(tester1);

    LocalNettyTester tester2 = createLocalNettyTester();
    tester2.connect(localAddress, new LocalAddress(TEST_ADDRESS_2));
    basicHandshake(tester2);

    Random random = new Random();
    byte[] messageId = new byte[16];
    String messageStr = "SUCCESS";
    random.nextBytes(messageId);

    BroadcastMessage message =
        BroadcastMessage
            .newBuilder()
            .setListeningAddress(1)
            .setListeningPort(43)
            .setHeader(
                Header.newBuilder().setHops(0).setId(ByteString.copyFrom(messageId)).setTtl(2))
            .setMessage(messageStr).build();
    tester1.writeAndWait(ChannelBuffers.wrappedBuffer(message.toByteArray()), 1000);


    BroadcastMessage received =
        BroadcastMessage
            .newBuilder()
            .setListeningAddress(1)
            .setListeningPort(43)
            .setHeader(
                Header.newBuilder().setHops(1).setId(ByteString.copyFrom(messageId)).setTtl(1))
            .setMessage(messageStr).build();

    tester2.verifyReceived(ChannelBuffers.wrappedBuffer(received.toByteArray()));
    tester1.verifyNothingReceived();

    tester1.verifyNotClosed();
    tester2.verifyNotClosed();
    bootstrap.shutdown(1000);
  }

  @Test
  public void testTimeBroadcast() {

    Injector inj =
        Guice.createInjector(Modules
            .override(new ProtobeeGuiceModule(), new BroadcastGuiceModule(), new EmotionGuiceModule()).with(
                new LocalChannelsModule()));

    ProtobeeServantBootstrapper bootstrap = inj.getInstance(ProtobeeServantBootstrapper.class);
    bootstrap.startup();

    LocalNettyTester tester1 = createLocalNettyTester();
    tester1.connect(new LocalAddress(LocalChannelsModule.LOCAL_ADDRESS), new LocalAddress(
        TEST_ADDRESS_1));
    timedHandshake(tester1);

    LocalNettyTester tester2 = createLocalNettyTester();
    tester2.connect(new LocalAddress(LocalChannelsModule.LOCAL_ADDRESS), new LocalAddress(
        TEST_ADDRESS_2));
    timedHandshake(tester2);

    Random random = new Random();
    byte[] messageId = new byte[16];
    String messageStr = "SUCCESS";
    long time = 19293l;
    random.nextBytes(messageId);

    BroadcastMessage message =
        BroadcastMessage
            .newBuilder()
            .setListeningAddress(1)
            .setListeningPort(43)
            .setHeader(
                Header.newBuilder().setHops(0).setId(ByteString.copyFrom(messageId)).setTtl(2))
            .setMessage(messageStr).setSendTimeMillis(time).build();
    tester1.writeAndWait(ChannelBuffers.wrappedBuffer(message.toByteArray()), 1000);

    BroadcastMessage received =
        BroadcastMessage
            .newBuilder()
            .setListeningAddress(1)
            .setListeningPort(43)
            .setHeader(
                Header.newBuilder().setHops(1).setId(ByteString.copyFrom(messageId)).setTtl(1))
            .setMessage(messageStr).setSendTimeMillis(time).build();

    tester2.verifyReceived(ChannelBuffers.wrappedBuffer(received.toByteArray()));
    tester1.verifyNothingReceived();

    tester1.verifyNotClosed();
    tester2.verifyNotClosed();
    bootstrap.shutdown(1000);

  }
}
