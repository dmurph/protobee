package org.protobee.guice.scopes;

import java.util.Map;

import com.google.common.collect.MapMaker;
import com.google.inject.Key;
import com.google.inject.Provider;

public class DefaultScopeMapProvider implements Provider<Map<Key<?>, Object>> {
  @Override
  public Map<Key<?>, Object> get() {
    return new MapMaker().concurrencyLevel(8).initialCapacity(100).makeMap();
  }
}
