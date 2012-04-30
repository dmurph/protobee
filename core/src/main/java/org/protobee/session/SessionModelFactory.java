package org.protobee.session;

import java.util.Map;

import org.protobee.guice.IdentityScope;
import org.protobee.guice.ProtobeeScopes;
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
    ProtobeeScopes.putObjectInScope(Key.get(EventBus.class), sessionEventBus, map);
    ProtobeeScopes.putObjectInScope(Key.get(Protocol.class), protocol, map);
    ProtobeeScopes.putObjectInScope(Key.get(ProtocolConfig.class), pconfig, map);
    ProtobeeScopes.enterSessionScope(map);

    ProtocolModulesHolder protocolSessionModel = pconfig.createSessionModel();
    ProtobeeScopes.putObjectInScope(Key.get(ProtocolModulesHolder.class), protocolSessionModel, map);
    
    SessionModel sessionModel = sessionModelProvider.get();
    
    sessionModel.getSessionScopeMap().putAll(map);

    ProtobeeScopes.exitSessionScope();
    return sessionModel;
  }
}
