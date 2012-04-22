package edu.cornell.jnutella.identity;

import java.net.SocketAddress;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Key;

import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.guice.IdentityScope;
import edu.cornell.jnutella.guice.IdentityScopeMap;
import edu.cornell.jnutella.guice.JnutellaScopes;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.protocol.ProtocolConfig;
import edu.cornell.jnutella.session.SessionModel;

@IdentityScope
public class NetworkIdentity {

  @InjectLogger
  private Logger log;
  private final Map<Protocol, ProtocolIdentityModel> protocolModels;
  private final Map<String, Object> identityScopeMap;
  private final Set<Object> tags = Sets.newHashSet();

  @Inject
  public NetworkIdentity(Map<Protocol, ProtocolConfig> configMap,
      @IdentityScopeMap Map<String, Object> identityScopeMap) {
    this.identityScopeMap = identityScopeMap;
    ImmutableMap.Builder<Protocol, ProtocolIdentityModel> builder = ImmutableMap.builder();

    for (Protocol protocol : configMap.keySet()) {
      ProtocolConfig config = configMap.get(protocol);
      ProtocolIdentityModel identityModel = config.createIdentityModel();
      if (identityModel == null) {
        log.warn("No model for protocol: " + protocol);
        continue;
      }
      builder.put(protocol, identityModel);
    }
    protocolModels = builder.build();
    // this should happen automatically because this object is in the identity scope, but we'll keep
    // this here in case of subclasses
    JnutellaScopes.putObjectInScope(Key.get(NetworkIdentity.class), this, identityScopeMap);
  }

  public ProtocolIdentityModel getModel(Protocol protocol) {
    return protocolModels.get(protocol);
  }

  public void clearCurrentSession(Protocol protocol) {
    protocolModels.get(protocol).clearCurrentSession();
  }

  public boolean hasCurrentSession(Protocol protocol) {
    return protocolModels.get(protocol).hasCurrentSession();
  }

  public SessionModel getCurrentSession(Protocol protocol) {
    return protocolModels.get(protocol).getCurrentSession();
  }

  /**
   * returns an immutable copy of this object's tags
   * 
   * @return
   */
  public Set<Object> getTags() {
    return ImmutableSet.copyOf(tags);
  }

  void addTag(Object tag) {
    tags.add(tag);
  }

  void setNewtorkAddress(Protocol protocol, SocketAddress address) {
    if (protocolModels.containsKey(protocol)) {
      protocolModels.get(protocol).setNetworkAddress(address);
    } else {
      log.error("No protocol model for protocol: " + protocol);
    }
  }

  Map<String, Object> getIdentityScopeMap() {
    return identityScopeMap;
  }

  @Override
  public final int hashCode() {
    return super.hashCode();
  }

  @Override
  public String toString() {
    return "{ tags: " + tags.toString() + ", protocolModels: " + protocolModels + "}";
  }

  public void enterScope() {
    JnutellaScopes.enterIdentityScope(identityScopeMap);
  }

  public boolean isInScope() {
    return JnutellaScopes.isInIdentityScope(identityScopeMap);
  }

  public void addObjectToScope(Key<?> key, Object object) {
    JnutellaScopes.putObjectInScope(key, object, identityScopeMap);
  }

  public void exitScope() {
    JnutellaScopes.exitIdentityScope();
  }
}
