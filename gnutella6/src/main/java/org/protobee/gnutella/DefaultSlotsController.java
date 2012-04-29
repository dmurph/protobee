package org.protobee.gnutella;

import org.protobee.gnutella.constants.MaxConnections;
import org.protobee.protocol.Protocol;
import org.protobee.session.SessionManager;

import com.google.inject.Inject;
import com.google.inject.Singleton;


@Singleton
public class DefaultSlotsController implements SlotsController {

  private final Protocol gnutella;
  private final SessionManager sessions;
  private final int maxConnections;

  @Inject
  public DefaultSlotsController(SessionManager sessions, @MaxConnections int maxConnections,
      @Gnutella Protocol protocol) {
    this.sessions = sessions;
    this.maxConnections = maxConnections;
    this.gnutella = protocol;
  }

  @Override
  public boolean canAcceptNewConnection() {
    return sessions.getCurrentSessionCount(gnutella) < maxConnections;
  }
}
