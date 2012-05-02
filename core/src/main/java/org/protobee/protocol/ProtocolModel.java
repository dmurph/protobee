package org.protobee.protocol;

import org.protobee.guice.scopes.ProtocolScope;
import org.protobee.guice.scopes.ProtocolScopeHolder;
import org.protobee.guice.scopes.ScopeHolder;

import com.google.inject.Inject;

/**
 * This stores protocol data and the protocol scope, mostly populated from the config. Ideally, we
 * shouldn't be storing the protocol here because it's in the scope already, but it cleans up a lot
 * of classes and it's used as a 'key' anyways.
 * 
 * @author Daniel
 */
@ProtocolScope
public class ProtocolModel {

  private final ScopeHolder scope;
  private final Protocol protocol;

  @Inject
  public ProtocolModel(@ProtocolScopeHolder ScopeHolder scopeHolder, Protocol protocol) {
    this.scope = scopeHolder;
    this.protocol = protocol;
  }

  public Protocol getProtocol() {
    return protocol;
  }

  public ScopeHolder getScope() {
    return scope;
  }

  public void enterScope() {
    scope.enterScope();
  }

  public boolean isInScope() {
    return scope.isInScope();
  }

  public void exitScope() {
    scope.exitScope();
  }
}
