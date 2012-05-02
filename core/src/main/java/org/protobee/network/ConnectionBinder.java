package org.protobee.network;

import java.net.SocketAddress;
import java.util.Set;

import org.jboss.netty.channel.Channel;
import org.protobee.protocol.ProtocolModel;


/**
 * Class for binding connections on servant startup
 * 
 * @author Daniel
 */
public interface ConnectionBinder {
  /**
   * Preconditions: local address from the config must not already be bound
   * 
   * @param config
   * @return
   */
  Channel bind(final ProtocolModel config);

  /**
   * Precondition: all the config's listening address has to match the local address
   * 
   * @param configs
   * @param localAddress
   * @return
   */
  Channel bind(final Set<ProtocolModel> configs, SocketAddress localAddress);
}
