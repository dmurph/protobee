package edu.cornell.jnutella.network;

import java.util.Set;

import org.jboss.netty.channel.Channel;

import edu.cornell.jnutella.protocol.ProtocolConfig;

/**
 * Class for binding connections on servant startup
 * 
 * @author Daniel
 */
public interface ConnectionBinder {
  /**
   * Preconditions: port from the config must not be already bound
   * 
   * @param config
   * @return
   */
  Channel bind(final ProtocolConfig config);

  /**
   * Precondition: all config's ports must match the the port argument, port > 0, and that port must
   * not be already bound
   * 
   * @param configs
   * @param port
   * @return
   */
  Channel bind(final Set<ProtocolConfig> configs, int port);
}
