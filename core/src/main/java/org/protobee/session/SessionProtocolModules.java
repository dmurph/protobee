package org.protobee.session;

import java.util.Set;

import org.protobee.guice.scopes.SessionScope;
import org.protobee.modules.ProtocolModule;
import org.protobee.session.handshake.SessionUpstreamHandshaker;

import com.google.common.collect.Sets;
import com.google.inject.Inject;


/**
 * Used by the framework to keep track of the loaded protocol modules for each session.
 * 
 * @author Daniel
 */
@SessionScope
public class SessionProtocolModules {

  private final Set<ProtocolModule> mutableModules;

  @Inject
  public SessionProtocolModules(Set<ProtocolModule> modules) {
    mutableModules = Sets.newHashSet(modules);
  }

  /**
   * Gets a mutable set of the protocol modules for this session, mutable so the
   * {@link SessionUpstreamHandshaker} can filter them based on compatibility
   * 
   * @return
   */
  public Set<ProtocolModule> getMutableModules() {
    return mutableModules;
  }
}
