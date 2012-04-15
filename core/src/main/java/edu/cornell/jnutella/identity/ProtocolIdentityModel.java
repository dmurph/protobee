package edu.cornell.jnutella.identity;

import java.net.SocketAddress;

public interface ProtocolIdentityModel {
  /**
   * Sets the network address for this protocol for this identity
   * 
   * @param address
   */
  void setNetworkAddress(SocketAddress address);

  /**
   * Gets the network address pertaining to this protocol for this identity
   * 
   * @return
   */
  SocketAddress getNetworkAddress();
}
