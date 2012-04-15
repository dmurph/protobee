package edu.cornell.jnutella.identity;

import java.net.SocketAddress;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;

import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.protocol.Protocol;

/**
 * Manages network identities
 * 
 * @author Daniel
 */
public class NetworkIdentityManager {

  @InjectLogger
  private Logger log;
  private final Map<Object, Set<NetworkIdentity>> taggedIdentities = Maps.newHashMap();
  private final Set<NetworkIdentity> identities = Sets.newHashSet();
  private final Map<SocketAddress, NetworkIdentity> identityLocationMap = Maps.newHashMap();
  private final Object identitiesLock = new Object();
  private final Provider<NetworkIdentity> identityProvider;

  @Inject
  public NetworkIdentityManager(Provider<NetworkIdentity> identityProvider) {
    this.identityProvider = identityProvider;
  }

  public NetworkIdentity createNetworkIdentity() {
    return identityProvider.get();
  }

  public boolean hasNetworkIdentity(SocketAddress address) {
    synchronized (identitiesLock) {
      return identityLocationMap.containsKey(address);
    }
  }

  public NetworkIdentity getNewtorkIdentity(SocketAddress address) {
    synchronized (identitiesLock) {
      return identityLocationMap.get(address);
    }
  }

  public Set<NetworkIdentity> getTaggedIdentities(Object tag) {
    Preconditions.checkNotNull(tag, "Tag cannot be null");
    synchronized (identitiesLock) {
      return ImmutableSet.copyOf(taggedIdentities.get(tag));
    }
  }

  public void tagIdentity(Object tag, NetworkIdentity identity) {
    Preconditions.checkNotNull(tag, "Tag cannot be null");
    Preconditions.checkNotNull(identity, "Identity cannot be null");
    synchronized (identitiesLock) {
      if (!identities.contains(identity)) {
        throw new IllegalStateException("Cannot tag an entity that isn't registered");
      }
      identity.addTag(tag);
      Set<NetworkIdentity> tagged = taggedIdentities.get(tag);
      if (tagged == null) {
        tagged = Sets.newHashSet();
        taggedIdentities.put(tag, tagged);
      }
      tagged.add(identity);
    }
  }

  public void setNetworkAddress(NetworkIdentity identity, Protocol protocol, SocketAddress address) {
    Preconditions.checkNotNull(identity, "Identity cannot be null");
    Preconditions.checkNotNull(protocol, "Protocol cannot be null");
    Preconditions.checkNotNull(address, "Address cannot be null");

    synchronized (identitiesLock) {
      if (!identities.contains(identity)) {
        throw new IllegalStateException("Cannot tag an entity that isn't registered");
      }

      if (identityLocationMap.containsKey(address)) {
        log.error("Address '" + address + "' already has an identity assigned to it.");
        throw new IllegalStateException("Address '" + address
            + "' already has an identity assigned to it.");
      }
      identityLocationMap.put(address, identity);
      identity.setNewtorkAddress(protocol, address);
    }
  }
}
