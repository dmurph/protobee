package org.protobee.util;

import com.google.inject.Singleton;

@Singleton
public class SystemClock implements Clock {

  @Override
  public long currentTimeMillis() {
    return System.currentTimeMillis();
  }

}
