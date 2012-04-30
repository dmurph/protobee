package org.protobee.session;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.protobee.protocol.Protocol;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;


@Singleton
public class SessionManagerImpl implements SessionManager {

  private final Map<Protocol, Set<SessionModel>> currentSessions;

  @Inject
  public SessionManagerImpl(Set<Protocol> protocols) {
    ImmutableMap.Builder<Protocol, Set<SessionModel>> builder = ImmutableMap.builder();
    for (Protocol protocol : protocols) {
      builder.put(protocol, Sets.<SessionModel>newHashSetWithExpectedSize(50));
    }
    this.currentSessions = builder.build();
  }

  @Override
  public void registerNewSession(Protocol protocol, SessionModel session) {
    Set<SessionModel> models = currentSessions.get(protocol);
    Preconditions.checkArgument(models != null, "Protocol not part of injected set");
    synchronized (models) {
      models.add(session);
    }
  }

  @Override
  public Set<SessionModel> getCurrentSessions(Protocol protocol) {
    Collection<SessionModel> models = currentSessions.get(protocol);
    Preconditions.checkArgument(models != null, "Protocol not part of injected set");
    synchronized (models) {
      return ImmutableSet.<SessionModel>builder().addAll(models).build();
    }
  }

  @Override
  public int getCurrentSessionCount(Protocol protocol) {
    Collection<SessionModel> models = currentSessions.get(protocol);
    Preconditions.checkArgument(models != null, "Protocol not part of injected set");
    return models.size();
  }

  @Override
  public boolean removeCurrentSession(Protocol protocol, SessionModel session) {
    Set<SessionModel> models = currentSessions.get(protocol);
    Preconditions.checkArgument(models != null, "Protocol not part of injected set");
    synchronized (models) {
      return models.remove(session);
    }
  }
}
