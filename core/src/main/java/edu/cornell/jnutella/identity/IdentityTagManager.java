package edu.cornell.jnutella.identity;

import java.util.Set;

import org.slf4j.Logger;

import com.google.common.collect.Sets;

import edu.cornell.jnutella.annotation.InjectLogger;

/**
 * Used to generate tag values for tagging network identities.
 * 
 * @author Daniel
 */
public class IdentityTagManager {

  @InjectLogger
  private Logger log;
  private final Set<Long> keySet;

  // 10 so we have room for our default keys
  private long startKey = 10;

  private Long ultrapeerKey;

  public IdentityTagManager() {
    keySet = Sets.newHashSet();
    ultrapeerKey = Long.valueOf(1);
    keySet.add(ultrapeerKey);
  }

  public synchronized Object generateKey(long requestedValue) {
    if (keySet.contains(requestedValue)) {
      long newValue = startKey++;
      log.warn("Key '" + requestedValue + "' already registered.  Returning '" + newValue
          + "' instead.");
      keySet.add(newValue);
      return Long.valueOf(newValue);
    }
    keySet.add(requestedValue);
    return Long.valueOf(requestedValue);
  }

  public Object getUltrapeerKey() {
    return ultrapeerKey;
  }
}