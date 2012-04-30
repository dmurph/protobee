package org.protobee.session;

import java.util.Map;

import org.protobee.guice.IdentityScope;
import org.protobee.guice.JnutellaScopes;
import org.protobee.protocol.Protocol;
import org.protobee.protocol.ProtocolConfig;

import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Provider;


/**
 * Creates a session for a protocol in an identity. Populates all seeded scoped objects
 * 
 * @author Daniel
 */
@IdentityScope
public class SessionModelFactory {

  private final Provider<SessionModel> sessionModelProvider;
  
  @Inject
  public SessionModelFactory(Provider<SessionModel> sessionModelProvider) {
    this.sessionModelProvider = sessionModelProvider;
  }

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

    ProtocolModulesHolder protocolSessionModel = pconfig.createSessionModel();
    JnutellaScopes.putObjectInScope(Key.get(ProtocolModulesHolder.class), protocolSessionModel, map);
    
    SessionModel sessionModel = sessionModelProvider.get();
    
    sessionModel.getSessionScopeMap().putAll(map);

    JnutellaScopes.exitSessionScope();
    return sessionModel;
  }
}
