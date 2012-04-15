package edu.cornell.jnutella.identity;

import java.net.SocketAddress;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.protocol.Protocol;
import edu.cornell.jnutella.protocol.ProtocolConfig;

public class NetworkIdentity {

  @InjectLogger
  private Logger log;
  private final Map<Protocol, ProtocolIdentityModel> protocolModels;

  private final Set<Object> tags = Sets.newHashSet();

  @Inject
  public NetworkIdentity(Set<ProtocolConfig> protocols) {

    ImmutableMap.Builder<Protocol, ProtocolIdentityModel> builder = ImmutableMap.builder();

    for (ProtocolConfig protocolConfig : protocols) {
      ProtocolIdentityModel identityModel = protocolConfig.createIdentityModel();
      Protocol protocol = protocolConfig.getClass().getAnnotation(Protocol.class);
      if (protocol == null) {
        log.error("Protocol config not annotated with protocol: " + protocolConfig);
        throw new IllegalArgumentException("Protocol config not annotated with protocol: "
            + protocolConfig);
      }
      if (identityModel == null) {
        log.debug("Protocol doesn't have identity model: " + protocol);
      }
      builder.put(protocol, identityModel);
    }
    protocolModels = builder.build();
  }

  public ProtocolIdentityModel getModel(Protocol protocol) {
    return protocolModels.get(protocol);
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
}
