package edu.cornell.jnutella.session;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import edu.cornell.jnutella.protocol.Protocol;

@Singleton
public class SessionManager {

  private final Map<Protocol, Set<SessionModel>> currentSessions;

  @Inject
  public SessionManager(Set<Protocol> protocols) {
    ImmutableMap.Builder<Protocol, Set<SessionModel>> builder = ImmutableMap.builder();
    for (Protocol protocol : protocols) {
      builder.put(protocol, Sets.<SessionModel>newHashSetWithExpectedSize(50));
    }
    this.currentSessions = builder.build();
  }

  public void registerNewSession(Protocol protocol, SessionModel session) {
    Set<SessionModel> models = currentSessions.get(protocol);
    Preconditions.checkArgument(models != null, "Protocol not part of injected set");
    synchronized (models) {
      models.add(session);
    }
  }

  public Set<SessionModel> getCurrentSessions(Protocol protocol) {
    Collection<SessionModel> models = currentSessions.get(protocol);
    Preconditions.checkArgument(models != null, "Protocol not part of injected set");
    synchronized (models) {
      return ImmutableSet.<SessionModel>builder().addAll(models).build();
    }
  }

  public int getCurrentSessionCount(Protocol protocol) {
    Collection<SessionModel> models = currentSessions.get(protocol);
    Preconditions.checkArgument(models != null, "Protocol not part of injected set");
    return models.size();
  }

  public boolean removeCurrentSession(Protocol protocol, SessionModel session) {
    Set<SessionModel> models = currentSessions.get(protocol);
    Preconditions.checkArgument(models != null, "Protocol not part of injected set");
    synchronized (models) {
      return models.remove(session);
    }
  }
}
