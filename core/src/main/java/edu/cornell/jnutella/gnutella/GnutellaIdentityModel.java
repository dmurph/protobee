package edu.cornell.jnutella.gnutella;

import java.net.SocketAddress;

import edu.cornell.jnutella.identity.ProtocolIdentityModel;

public class GnutellaIdentityModel implements ProtocolIdentityModel {

  private SocketAddress address;
  private byte[] guid;

  @Override
  public void setNetworkAddress(SocketAddress address) {
    this.address = address;
  }

  @Override
  public SocketAddress getNetworkAddress() {
    return address;
  }

  public byte[] getGuid() {
    return guid;
  }

  public void setGuid(byte[] guid) {
    this.guid = guid;
  }
}
