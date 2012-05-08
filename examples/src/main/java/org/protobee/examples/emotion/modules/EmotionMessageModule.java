package org.protobee.examples.emotion.modules;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.protobee.annotation.InjectLogger;
import org.protobee.compatability.CompatabilityHeader;
import org.protobee.compatability.Headers;
import org.protobee.events.BasicMessageReceivedEvent;
import org.protobee.examples.protos.Common.Header;
import org.protobee.examples.protos.EmotionProtos.EmotionMessage;
import org.protobee.examples.protos.EmotionProtos.EmotionMessage.Type;
import org.protobee.guice.scopes.SessionScope;
import org.protobee.identity.NetworkIdentity;
import org.protobee.modules.ProtocolModule;
import org.protobee.network.ProtobeeMessageWriter;
import org.protobee.network.ProtobeeMessageWriter.HandshakeOptions;
import org.protobee.protocol.Protocol;
import org.protobee.session.SessionManager;
import org.protobee.session.SessionModel;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.protobuf.ByteString;

@SessionScope
@Headers(required = {}, silentExcluding={@CompatabilityHeader(name="Time-Support", minVersion="0.1", maxVersion="+")})
public class EmotionMessageModule extends ProtocolModule {

  @InjectLogger
  private Logger log;
  private final SessionManager sessionManager;
  private final SessionModel session;
  private final NetworkIdentity identity;
  private final Protocol myProtocol;
  private final ProtobeeMessageWriter writer;

  @Inject
  public EmotionMessageModule(SessionManager sessionManager, SessionModel session,
                              NetworkIdentity identity, Protocol protocol, ProtobeeMessageWriter writer) {
    this.sessionManager = sessionManager;
    this.session = session;
    this.identity = identity;
    this.myProtocol = protocol;
    this.writer = writer;
  }

  @Subscribe
  public void messageRecieved(BasicMessageReceivedEvent event) {
    Preconditions.checkArgument(event.getMessage() instanceof EmotionMessage,
        "Not an emotion message");

    EmotionMessage message = (EmotionMessage) event.getMessage();
    
    log.info("Received message " + message);

    SimpleThread thread = new SimpleThread();
    thread.start();

//    session.
//    
//    protocol.handler
//    sessionclosingposter
    
    
    
    session.exitScope();
    identity.exitScope();
  }
  
  class SimpleThread extends Thread {
    protected final List<Type> values;
    protected final Random random;
    public SimpleThread() {
      super();
      values = Collections.unmodifiableList(Arrays.asList(Type.values()));
      random = new Random();
    }
    public void run() {

      //TODO
      ByteString id = ByteString.copyFrom(new byte[4]);
      
      EmotionMessage sendingMessage =
          EmotionMessage.newBuilder()
          .setHeader(Header.newBuilder().setTtl(1).setHops(0).setId(id))
          .setEmotion(values.get(random.nextInt(values.size()))).build();
      log.info("Sending message " + sendingMessage + " to "
          + session.getIdentity().getListeningAddress(myProtocol));
      writer.write(sendingMessage, HandshakeOptions.WAIT_FOR_HANDSHAKE);

    }
  }
}


