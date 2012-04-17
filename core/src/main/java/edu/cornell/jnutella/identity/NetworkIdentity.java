package edu.cornell.jnutella.identity;

import java.net.SocketAddress;
import java.util.Map;
import java.util.Set;

import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.protocol.ProtocolConfig;
import edu.cornell.jnutella.protocol.session.SessionModel;

public class NetworkIdentity {

  @InjectLogger
  private Logger log;
  private final Map<Protocol, ProtocolIdentityModel> protocolModels;
  private final Map<Protocol, ProtocolConfig> protocolConfigs;

  private final Set<Object> tags = Sets.newHashSet();

  @Inject
  public NetworkIdentity(Map<Protocol, ProtocolConfig> configMap) {
    protocolConfigs = configMap;

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

  public void createNewSession(Channel channel, Protocol protocol) {
    Preconditions.checkArgument(protocolModels.containsKey(protocol), "No model for protocol "
        + protocol);
    ProtocolIdentityModel model = protocolModels.get(protocol);
    if (model.hasCurrentSession()) {
      log.warn("Protocol " + protocol + " already has a current session.");
    }
    model.setCurrentSessionModel(protocolConfigs.get(protocol).createSessionModel(channel, this));
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

  @Override
  public final int hashCode() {
    return super.hashCode();
  }

  @Override
  public String toString() {
    return "{ tags: " + tags.toString() + ", protocolModels: " + protocolModels + "}";
  }
}
