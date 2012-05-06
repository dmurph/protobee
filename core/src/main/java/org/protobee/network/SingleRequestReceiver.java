package org.protobee.network;

import org.protobee.identity.NetworkIdentityManager;
import org.protobee.protocol.ProtocolConfig;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;


/**
 * Sets up a protocol session when a request is received, where we're only watching for one protocol
 * 
 * @author Daniel
 */
public class SingleRequestReceiver extends AbstractRequestReceiver {

  public static interface Factory {
    SingleRequestReceiver create(ProtocolConfig protocol);
  }

  private final ProtocolConfig protocolConfig;

  @AssistedInject
  public SingleRequestReceiver(@Assisted ProtocolConfig protocol,
      HandshakeStateBootstrapper handshakeBootstrap, NetworkIdentityManager identityManager,
      JnutellaChannels channels) {
    super(handshakeBootstrap, identityManager, channels);
    this.protocolConfig = protocol;
  }

  @Override
  protected ProtocolConfig getMatchingConfig(String header) {
    return header.matches(protocolConfig.get().headerRegex()) ? protocolConfig : null;
  }
}