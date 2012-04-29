package org.protobee.session;

import java.util.Set;

import org.protobee.modules.ProtocolModule;


public interface ProtocolSessionModel {
  /**
   * Gets a mutable set of the protocol modules for this session, mutable so the
   * {@link SessionUpstreamHandshaker} can filter them based on compatibility
   * 
   * @return
   */
  Set<ProtocolModule> getMutableModules();
}
