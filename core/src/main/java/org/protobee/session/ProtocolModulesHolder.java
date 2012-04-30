package org.protobee.session;

import java.util.Set;

import org.protobee.modules.ProtocolModule;
import org.protobee.session.handshake.SessionUpstreamHandshaker;


/**
 * Used by the framework to keep track of the loaded protocol modules for each session.
 * 
 * @author Daniel
 */
public interface ProtocolModulesHolder {
  /**
   * Gets a mutable set of the protocol modules for this session, mutable so the
   * {@link SessionUpstreamHandshaker} can filter them based on compatibility
   * 
   * @return
   */
  Set<ProtocolModule> getMutableModules();
}
