package edu.cornell.jnutella.network;

import java.util.Set;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import edu.cornell.jnutella.identity.NetworkIdentityManager;
import edu.cornell.jnutella.protocol.ProtocolConfig;

/**
 * Sets up a protocol session when a request is received, where we're only watching for multiple
 * protocols
 * 
 * @author Daniel
 */
public class MultipleRequestReceiver extends AbstractRequestReceiver {

  public static interface Factory {
    MultipleRequestReceiver create(Set<ProtocolConfig> protocolsMultiplexing);
  }

  private final Set<ProtocolConfig> protocols;

  @AssistedInject
  public MultipleRequestReceiver(@Assisted Set<ProtocolConfig> protocols,
      HandshakeStateBootstrapper handshakeBootstrap, NetworkIdentityManager identityManager,
      JnutellaChannels channels) {
    super(handshakeBootstrap, identityManager, channels);
    this.protocols = protocols;
  }

  @Override
  protected ProtocolConfig getMatchingConfig(String header) {
    for (ProtocolConfig protocol : protocols) {
      if (header.matches(protocol.get().headerRegex())) {
        return protocol;
      }
    }
    return null;
  }
}
