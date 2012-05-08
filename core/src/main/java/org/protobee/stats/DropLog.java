package org.protobee.stats;

import java.net.SocketAddress;

import org.protobee.protocol.Protocol;


/**
 * Used for logging disconnections and message drops
 * 
 * @author Daniel
 */
public interface DropLog {
  void connectionDisconnecting(SocketAddress address, Protocol protocol, String reason);

  void connectionDisconnected(SocketAddress address, Protocol protocol);

  void messageDropped(SocketAddress address, Protocol protocol, Object message, String reason);
}
