package org.protobee.network.handlers;

import java.util.Set;

import org.protobee.identity.NetworkIdentityManager;
import org.protobee.network.ProtobeeChannels;
import org.protobee.protocol.ProtocolModel;
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
    MultipleRequestReceiver create(Set<ProtocolModel> protocolsMultiplexing);
  }

  private final Set<ProtocolModel> protocols;

  @AssistedInject
  public MultipleRequestReceiver(@Assisted Set<ProtocolModel> protocols,
      HandshakeStateBootstrapper handshakeBootstrap, NetworkIdentityManager identityManager,
      ProtobeeChannels channels) {
    super(handshakeBootstrap, identityManager, channels);
    this.protocols = protocols;
  }

  @Override
  protected ProtocolModel getMatchingConfig(String header) {
    for (ProtocolModel protocol : protocols) {
      if (header.matches(protocol.getProtocol().headerRegex())) {
        return protocol;
      }
    }
    return null;
  }
}
