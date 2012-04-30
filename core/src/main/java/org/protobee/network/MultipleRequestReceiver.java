package org.protobee.network;

import java.util.Set;

import org.protobee.identity.NetworkIdentityManager;
import org.protobee.protocol.ProtocolConfig;
import org.protobee.session.handshake.HandshakeStateBootstrapper;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;


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
      ProtobeeChannels channels) {
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
