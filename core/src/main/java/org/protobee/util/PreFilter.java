package org.protobee.util;


/**
 * Used to prefilter messages before they reach the modules
 * 
 * @author Daniel
 */
public interface PreFilter<T> {
  /**
   * If the message should be filtered.
   * 
   * @param message
   * @return the reason the message should be filtered, or null otherwise
   */
  String shouldFilter(T message);
}
