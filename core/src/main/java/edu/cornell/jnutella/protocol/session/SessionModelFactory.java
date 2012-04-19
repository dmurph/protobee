package edu.cornell.jnutella.protocol.session;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.inject.Key;

import edu.cornell.jnutella.guice.IdentityScope;
import edu.cornell.jnutella.guice.JnutellaScopes;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.protocol.ProtocolConfig;

/**
 * Creates a session for a protocol in an identity. Populates all seeded scoped objects
 * 
 * @author Daniel
 */
@IdentityScope
public class SessionModelFactory {


  /**
   * Precondition: was have to be in the respective entity scope
   * 
   * @param pconfig
   * @param eventBusSublabel
   * @return
   */
  public SessionModel create(ProtocolConfig pconfig, String eventBusSublabel) {
    Protocol protocol = pconfig.get();
    Map<String, Object> map = Maps.newHashMap();
    EventBus sessionEventBus = new EventBus(protocol.name() + "-" + eventBusSublabel);
    JnutellaScopes.putObjectInScope(Key.get(EventBus.class), sessionEventBus, map);
    JnutellaScopes.putObjectInScope(Key.get(Protocol.class), protocol, map);
    JnutellaScopes.putObjectInScope(Key.get(ProtocolConfig.class), pconfig, map);
    JnutellaScopes.enterSessionScope(map);

    SessionModel sessionModel = pconfig.createSessionModel();

    sessionModel.getSessionScopeMap().putAll(map);

    JnutellaScopes.exitSessionScope();
    return sessionModel;
  }
}
