package org.protobee.examples.emotion.modules;

import org.protobee.annotation.InjectLogger;
import org.protobee.compatability.Headers;
import org.protobee.events.BasicMessageReceivedEvent;
import org.protobee.events.ProtocolHandlersLoadedEvent;
import org.protobee.events.SessionClosingEvent;
import org.protobee.examples.emotion.Emotion;
import org.protobee.examples.emotion.EmotionProvider;
import org.protobee.examples.protos.Common.Header;
import org.protobee.examples.protos.EmotionProtos.EmotionMessage;
import org.protobee.guice.scopes.SessionScope;
import org.protobee.identity.NetworkIdentity;
import org.protobee.modules.ProtocolModule;
import org.protobee.network.ProtobeeMessageWriter;
import org.protobee.network.ProtobeeMessageWriter.HandshakeOptions;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.ProtocolModel;
import org.protobee.session.SessionModel;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

@SessionScope
@Headers(required = {})
public class EmotionMessageModule extends ProtocolModule implements Runnable {

  @InjectLogger
  private Logger log;
  private final SessionModel session;
  private final ProtocolModel protocolModel;
  private final Protocol myProtocol;
  private final ProtobeeMessageWriter writer;
  private final NetworkIdentity identity;
  private final EmotionProvider emotionProvider;

  private Thread thread;
  private boolean running = false;

  @Inject
  public EmotionMessageModule(SessionModel session, @Emotion Protocol protocol,
      ProtobeeMessageWriter writer, NetworkIdentity identity, EmotionProvider emotionProvider,
      ProtocolModel protocolModel) {
    this.session = session;
    this.myProtocol = protocol;
    this.writer = writer;
    this.identity = identity;
    this.emotionProvider = emotionProvider;
    this.protocolModel = protocolModel;
  }

  @Subscribe
  public void protocolLoaded(ProtocolHandlersLoadedEvent event) {
    thread = new Thread(this, "Demo Thread");
    thread.start();
  }

  @Subscribe
  public void channelDisconnected(SessionClosingEvent event) {
    stopThread();
  }

  @Override
  public void run() {
    log.info("run");
    running = true;
    try {
      protocolModel.enterScope();
      identity.enterScope();

      while (running) {
        EmotionMessage sendingMessage =
            EmotionMessage
                .newBuilder()
                .setHeader(
                    Header.newBuilder().setTtl(1).setHops(0)
                        .setId(emotionProvider.getNewEmotionId()))
                .setEmotion(emotionProvider.getEmotion()).build();
        log.info("SENDING emotion message " + sendingMessage + " to "
            + session.getIdentity().getListeningAddress(myProtocol));
        writer.write(sendingMessage, HandshakeOptions.WAIT_FOR_HANDSHAKE);
        try {
          Thread.sleep(5000);
        } catch (InterruptedException e) {}
      }

    } finally {
      protocolModel.exitScope();
      identity.exitScope();
    }
  }

  public void stopThread() {
    log.info("Stoping emotion thread");
    running = false;
  }

  @Subscribe
  public void messageRecieved(BasicMessageReceivedEvent event) {
    Preconditions.checkArgument(event.getMessage() instanceof EmotionMessage,
        "Not an emotion message");
    EmotionMessage message = (EmotionMessage) event.getMessage();
    log.info("RECEIVED emotion message " + message);
  }

}
