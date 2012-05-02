package org.protobee.guice.scopes;

import java.util.Map;

import com.google.common.collect.MapMaker;
import com.google.inject.Provider;

public class DefaultScopeMapProvider implements Provider<Map<String, Object>> {
  @Override
  public Map<String, Object> get() {
    return new MapMaker().concurrencyLevel(8).makeMap();
  }
}
