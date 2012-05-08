package org.protobee.examples.broadcast.modules;

import java.util.Set;

import org.protobee.annotation.InjectLogger;
import org.protobee.compatability.CompatabilityHeader;
import org.protobee.compatability.Headers;
import org.protobee.events.BasicMessageReceivedEvent;
import org.protobee.events.BasicMessageSendingEvent;
import org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage;
import org.protobee.examples.protos.Common.Header;
import org.protobee.guice.scopes.SessionScope;
import org.protobee.identity.NetworkIdentity;
import org.protobee.modules.ProtocolModule;
import org.protobee.network.ProtobeeMessageWriter;
import org.protobee.network.ProtobeeMessageWriter.HandshakeOptions;
import org.protobee.protocol.Protocol;
import org.protobee.session.SessionManager;
import org.protobee.session.SessionModel;
import org.protobee.util.Clock;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.protobuf.ByteString;

@SessionScope
@Headers(required = {@CompatabilityHeader(name = "Time-Support", maxVersion = "0.1", minVersion = "0.1")})
public class TimeBroadcastMessageModule extends ProtocolModule {

  @InjectLogger
  private Logger log;
  private final SessionManager sessionManager;
  private final SessionModel session;
  private final NetworkIdentity identity;
  private final Protocol myProtocol;
  private final ProtobeeMessageWriter writer;
  private final Clock clock;

  @Inject
  public TimeBroadcastMessageModule(SessionManager sessionManager, SessionModel session,
      NetworkIdentity identity, Protocol protocol, ProtobeeMessageWriter writer,
      Clock clock) {
    this.sessionManager = sessionManager;
    this.session = session;
    this.identity = identity;
    this.myProtocol = protocol;
    this.writer = writer;
    this.clock = clock;
  }
  
  @Subscribe
  public void sendingMessage(BasicMessageSendingEvent event) {
    Preconditions.checkArgument(event.getMessage() instanceof BroadcastMessage.Builder,
    "Not a broadcast message builder");
    BroadcastMessage.Builder message = (BroadcastMessage.Builder) event.getMessage();
    
    if(!message.hasSendTimeMillis()) {
      message.setSendTimeMillis(clock.currentTimeMillis());
    }
  }

  @Subscribe
  public void messageRecieved(BasicMessageReceivedEvent event) {
    Preconditions.checkArgument(event.getMessage() instanceof BroadcastMessage,
        "Not a broadcast message");

    BroadcastMessage message = (BroadcastMessage) event.getMessage();
    Header header = message.getHeader();
    int hops = header.getHops();
    int ttl = header.getTtl();
    long time = message.getSendTimeMillis();
    ByteString id = header.getId();

    log.info("Received message " + message);

    Set<SessionModel> sessions = sessionManager.getCurrentSessions(myProtocol);

    session.exitScope();
    identity.exitScope();

    BroadcastMessage.Builder sendingMessage =
        BroadcastMessage.newBuilder()
            .setHeader(Header.newBuilder().setTtl(ttl - 1).setHops(hops + 1).setId(id))
            .setMessage(message.getMessage())
            .setSendTimeMillis(time)
            .setListeningAddress(message.getListeningAddress())
            .setListeningPort(message.getListeningPort());

    for (SessionModel sessionModel : sessions) {
      if (sessionModel == session) {
        continue;
      }
      try {
        sessionModel.getIdentity().enterScope();
        log.info("Sending message " + sendingMessage.clone().buildPartial() + " to "
            + sessionModel.getIdentity().getListeningAddress(myProtocol));
        writer.write(sendingMessage, HandshakeOptions.WAIT_FOR_HANDSHAKE);
      } finally {
        sessionModel.getIdentity().exitScope();
      }
    }
    identity.enterScope();
    session.enterScope();
  }
}
