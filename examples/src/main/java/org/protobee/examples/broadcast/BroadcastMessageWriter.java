package org.protobee.examples.broadcast;

import java.util.Set;

import org.protobee.annotation.InjectLogger;
import org.protobee.examples.protos.BroadcasterProtos.BroadcastMessage;
import org.protobee.identity.NetworkIdentityManager;
import org.protobee.network.ProtobeeMessageWriter;
import org.protobee.network.ProtobeeMessageWriter.HandshakeOptions;
import org.protobee.protocol.ProtocolModel;
import org.protobee.session.SessionManager;
import org.protobee.session.SessionModel;
import org.slf4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class BroadcastMessageWriter {

  @InjectLogger
  private Logger log;
  private final SessionManager sessionManager;
  private final NetworkIdentityManager identityManager;
  private final ProtocolModel broadcast;
  private final ProtobeeMessageWriter writer;

  @Inject
  public BroadcastMessageWriter(SessionManager sessionManager,
      NetworkIdentityManager identityManager, @Broadcast ProtocolModel protocol,
      ProtobeeMessageWriter writer) {
    this.sessionManager = sessionManager;
    this.identityManager = identityManager;
    this.broadcast = protocol;
    this.writer = writer;
  }

  public void broadcastMessage(BroadcastMessage.Builder message) {
    try {
      broadcast.enterScope();
      Set<SessionModel> sessions = sessionManager.getCurrentSessions(broadcast.getProtocol());

      for (SessionModel sessionModel : sessions) {
        if (sessionModel.getIdentity() == identityManager.getMe()) {
          continue;
        }
        log.info("Sending message: " + message.clone().buildPartial());
        try {
          sessionModel.getIdentity().enterScope();
          writer.write(message, HandshakeOptions.WAIT_FOR_HANDSHAKE);
        } finally {
          sessionModel.getIdentity().exitScope();
        }
      }
    } finally {
      broadcast.exitScope();
    }
  }
}
