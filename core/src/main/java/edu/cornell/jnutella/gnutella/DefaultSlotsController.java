package edu.cornell.jnutella.gnutella;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import edu.cornell.jnutella.gnutella.constants.MaxConnections;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.session.SessionManager;

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
