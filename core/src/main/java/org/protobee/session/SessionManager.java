package org.protobee.session;

import java.util.Set;

import org.protobee.protocol.Protocol;



public interface SessionManager {

  public abstract void registerNewSession(Protocol protocol, SessionModel session);

  public abstract Set<SessionModel> getCurrentSessions(Protocol protocol);

  public abstract int getCurrentSessionCount(Protocol protocol);

  public abstract boolean removeCurrentSession(Protocol protocol, SessionModel session);

}
