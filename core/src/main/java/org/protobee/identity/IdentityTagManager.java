package org.protobee.identity;

import java.util.Set;

import org.protobee.annotation.InjectLogger;
import org.slf4j.Logger;

import com.google.common.collect.Sets;
import com.google.inject.Singleton;


/**
 * Used to generate tag values for tagging network identities.
 * 
 * @author Daniel
 */
@Singleton
public class IdentityTagManager {

  @InjectLogger
  private Logger log;
  private final Set<Long> keySet;

  // 10 so we have room for our default keys
  private long startKey = 10;

  private final Long ultrapeerKey;
  private final Long leafKey;

  public IdentityTagManager() {
    keySet = Sets.newHashSet();
    ultrapeerKey = Long.valueOf(1);
    leafKey = Long.valueOf(2);
    keySet.add(ultrapeerKey);
  }

  public synchronized Object generateKey(long requestedValue) {
    if (keySet.contains(requestedValue)) {
      long newValue = startKey++;
      while (keySet.contains(newValue)) {
        newValue = startKey++;
      }
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
  
  public Object getLeafKey() {
    return leafKey;
  }
}
