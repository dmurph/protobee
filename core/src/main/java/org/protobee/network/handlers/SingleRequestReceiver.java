package org.protobee.network.handlers;

import org.protobee.identity.NetworkIdentityManager;
import org.protobee.network.ProtobeeChannels;
import org.protobee.protocol.ProtocolModel;
import org.protobee.session.handshake.HandshakeStateBootstrapper;

import com.google.inject.Inject;

/**
 * Sets up a protocol session when a request is received, where we're only watching for one protocol
 * 
 * @author Daniel
 */
public class SingleRequestReceiver extends AbstractRequestReceiver {

  private final ProtocolModel protocolConfig;

  @Inject
  public SingleRequestReceiver(ProtocolModel protocol,
      HandshakeStateBootstrapper handshakeBootstrap, NetworkIdentityManager identityManager,
      ProtobeeChannels channels) {
    super(handshakeBootstrap, identityManager, channels);
    this.protocolConfig = protocol;
  }

  @Override
  protected ProtocolModel getMatchingConfig(String header) {
    return header.matches(protocolConfig.getProtocol().headerRegex()) ? protocolConfig : null;
  }
}
