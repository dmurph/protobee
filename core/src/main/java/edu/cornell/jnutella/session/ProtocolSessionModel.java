package edu.cornell.jnutella.session;

import java.util.Set;

import edu.cornell.jnutella.modules.ProtocolModule;

public interface ProtocolSessionModel {
  /**
   * Gets a mutable set of the protocol modules for this session, mutable so the
   * {@link SessionUpstreamHandshaker} can filter them based on compatibility
   * 
   * @return
   */
  Set<ProtocolModule> getMutableModules();
}
