package org.protobee.guice;

/**
 * Describes an object that contains a scope, which can be entered and exited.
 * 
 * @author Daniel
 */
public interface ScopeHolder {

  /**
   * Enters the scope of this object for the calling thread. All injections of objects that are
   * under the scope this object describes will be held by this object.
   * 
   * @throws IllegalStateException if we are already in the scope described by this object
   */
  void enterScope() throws IllegalStateException;

  /**
   * @return if the calling thread is currently in this object's scope
   */
  boolean isInScope();

  /**
   * Exits the scope this object holds.
   */
  void exitScope();
}
