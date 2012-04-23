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
import com.google.inject.Singleton;

import edu.cornell.jnutella.annotation.InjectLogger;
import edu.cornell.jnutella.protocol.Protocol;

/**
 * Manages network identities
 * 
 * @author Daniel
 */
@Singleton
public class NetworkIdentityManager {

  @InjectLogger
  private Logger log;
  private final Map<Object, Set<NetworkIdentity>> taggedIdentities = Maps.newHashMap();
  private final Set<NetworkIdentity> identities = Sets.newHashSet();
  private final Map<SocketAddress, NetworkIdentity> identityLocationMap = Maps.newHashMap();
  private final Object identitiesLock = new Object();
  private final NetworkIdentityFactory identityFactory;
  private final NetworkIdentity me;

  @Inject
  public NetworkIdentityManager(NetworkIdentityFactory identityFactory) {
    this.identityFactory = identityFactory;
    me = identityFactory.create();
    me.setDescription("Me");
    identities.add(me);
  }

  public NetworkIdentity createNetworkIdentity() {
    NetworkIdentity identity = identityFactory.create();
    synchronized (identitiesLock) {
      identities.add(identity);
    }
    return identity;
  }

  /**
   * Gets or creates a network identity with a new connection with the respective protocol.
   * 
   * @param protocol
   * @param address
   * @throws IllegalStateException if there is a current session for that protocol in the identity
   * @return
   */
  public NetworkIdentity getNetworkIdentityWithNewConnection(Protocol protocol,
      SocketAddress address) {
    Preconditions.checkNotNull(protocol);
    Preconditions.checkNotNull(address);
    NetworkIdentity identity;
    synchronized (identitiesLock) {
      identity = getNewtorkIdentity(address);
      if (identity != null) {
        Preconditions.checkState(!identity.hasCurrentSession(protocol),
            "Identity already has a current session");
      } else {
        identity = createNetworkIdentity();
      }
      setNetworkAddress(identity, protocol, address);
    }

    return identity;
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

  public NetworkIdentity getMe() {
    return me;
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
