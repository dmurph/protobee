package org.protobee.network;

import java.net.SocketAddress;

import org.jboss.netty.channel.ChannelFuture;
import org.protobee.protocol.Protocol;



public interface ConnectionCreator {

  /**
   * Connects to the given address with the given protocol. Preconditions: There must not a current
   * session for the given protocol.
   * 
   * @param protocol
   * @param remoteAddress
   * @return
   */
  ChannelFuture connect(final Protocol protocol, final SocketAddress remoteAddress);

  /**
   * Disconnects the protocol connected to the given remote address
   * 
   * @param protocol
   * @param remoteAddress
   * @return
   */
  ChannelFuture disconnect(final Protocol protocol, SocketAddress remoteAddress);
}
