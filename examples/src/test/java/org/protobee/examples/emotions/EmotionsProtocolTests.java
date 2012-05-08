package org.protobee.examples.emotions;

import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Random;


import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.local.LocalAddress;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.protobee.ProtobeeGuiceModule;
import org.protobee.ProtobeeServantBootstrapper;
import org.protobee.events.BasicMessageReceivedEvent;
import org.protobee.examples.emotion.EmotionGuiceModule;
import org.protobee.examples.emotion.EmotionProvider;
import org.protobee.examples.emotion.modules.EmotionMessageModule;
import org.protobee.examples.protos.Common.Header;
import org.protobee.examples.protos.EmotionProtos.EmotionMessage;
import org.protobee.examples.protos.EmotionProtos.EmotionMessage.Type;
import org.protobee.netty.LocalNettyTester;
import org.protobee.network.ProtobeeMessageWriter.HandshakeOptions;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.google.protobuf.ByteString;

public class EmotionsProtocolTests extends AbstractEmotionsTest {

  private static final String TEST_ADDRESS = "32.123.5.3:234";

  @Test
  public void testEmotion() throws Exception {

    byte[] id = new byte[16];
    new Random().nextBytes(id);
    byte[] id2 = new byte[16];
    new Random().nextBytes(id);
    
    final EmotionProvider emotionProvider = mock(EmotionProvider.class);
    when(emotionProvider.getEmotion()).thenReturn(Type.SAD, Type.AFRAID);
    when(emotionProvider.getNewEmotionId()).thenReturn(ByteString.copyFrom(id), ByteString.copyFrom(id2));
    
    Injector inj =
        Guice.createInjector(Modules
          .override(new ProtobeeGuiceModule(), new EmotionGuiceModule()).with(
            new LocalChannelsModule(), new AbstractModule() {
              @Override
              protected void configure() {
                bind(EmotionProvider.class).toInstance(emotionProvider);
              }
            }));

    ProtobeeServantBootstrapper bootstrap = inj.getInstance(ProtobeeServantBootstrapper.class);
    bootstrap.startup();
    
    LocalAddress localAddress = new LocalAddress(LocalChannelsModule.LOCAL_ADDRESS);
    LocalNettyTester tester = createLocalNettyTester();
    tester.connect(localAddress, new LocalAddress(TEST_ADDRESS));

    basicHandshake(tester);

    Thread.sleep(100);
    EmotionMessage received =
        EmotionMessage
        .newBuilder()
        .setEmotion(Type.SAD)
        .setHeader(Header.newBuilder().setHops(0).setId(ByteString.copyFrom(id)).setTtl(1)).build();
    
    tester.verifyReceived(ChannelBuffers.wrappedBuffer(received.toByteArray()));

    tester.clearReceived();
    Thread.sleep(5000);
    
    EmotionMessage received2 =
        EmotionMessage
        .newBuilder()
        .setEmotion(Type.AFRAID)
        .setHeader(Header.newBuilder().setHops(0).setId(ByteString.copyFrom(id2)).setTtl(1)).build();
    
    tester.verifyReceived(ChannelBuffers.wrappedBuffer(received2.toByteArray()));
    
    tester.verifyNotClosed();
    bootstrap.shutdown(1000);
  }

}
