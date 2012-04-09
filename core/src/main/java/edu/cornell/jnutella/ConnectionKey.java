package edu.cornell.jnutella;

import java.net.SocketAddress;

public class ConnectionKey {

  private final SocketAddress localAddress;
  private final SocketAddress remoteAddress;

  public ConnectionKey(SocketAddress localAddress, SocketAddress remoteAddress) {
    this.localAddress = localAddress;
    this.remoteAddress = remoteAddress;
  }

  public SocketAddress getLocalAddress() {
    return localAddress;
  }

  public SocketAddress getRemoteAddress() {
    return remoteAddress;
  }
}
