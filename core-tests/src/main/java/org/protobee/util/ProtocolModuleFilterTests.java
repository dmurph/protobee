package org.protobee.util;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.protobee.modules.ProtocolModule;
import org.protobee.protocol.headers.CompatabilityHeader;
import org.protobee.protocol.headers.Headers;
import org.protobee.util.ProtocolModuleFilter;
import org.protobee.util.VersionComparator;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;


public class ProtocolModuleFilterTests {

  @Headers(required = {@CompatabilityHeader(name = "a", minVersion = "1", maxVersion = "2")}, requested = {})
  private static class a12 implements ProtocolModule {}

  @Headers(required = {@CompatabilityHeader(name = "a", minVersion = "2", maxVersion = "+")}, requested = {})
  private static class a2plus implements ProtocolModule {}

  @Test
  public void testLowerVersion() {
    ProtocolModuleFilter filter = new ProtocolModuleFilter(new VersionComparator());
    Set<ProtocolModule> modules = Sets.newHashSet((ProtocolModule) new a12());

    Map<String, String> httpHeaders = Maps.newHashMap();
    httpHeaders.put("a", "0");
    filter.filterModules(modules, httpHeaders, new Predicate<ProtocolModule>() {
      @Override
      public boolean apply(ProtocolModule input) {
        return true;
      }
    });

    assertEquals(0, modules.size());
  }

  @Test
  public void testRequiredNotPresent() {
    ProtocolModuleFilter filter = new ProtocolModuleFilter(new VersionComparator());
    Set<ProtocolModule> modules = Sets.newHashSet((ProtocolModule) new a12());

    Map<String, String> httpHeaders = Maps.newHashMap();
    filter.filterModules(modules, httpHeaders, new Predicate<ProtocolModule>() {
      @Override
      public boolean apply(ProtocolModule input) {
        return true;
      }
    });

    assertEquals(0, modules.size());
  }

  @Test
  public void testToHigh() {
    ProtocolModuleFilter filter = new ProtocolModuleFilter(new VersionComparator());
    Set<ProtocolModule> modules = Sets.newHashSet((ProtocolModule) new a12());

    Map<String, String> httpHeaders = Maps.newHashMap();
    httpHeaders.put("a", "3");
    filter.filterModules(modules, httpHeaders, new Predicate<ProtocolModule>() {
      @Override
      public boolean apply(ProtocolModule input) {
        return true;
      }
    });

    assertEquals(0, modules.size());
  }


  @Test
  public void testFits() {
    ProtocolModuleFilter filter = new ProtocolModuleFilter(new VersionComparator());
    Set<ProtocolModule> modules = Sets.newHashSet((ProtocolModule) new a12());

    Map<String, String> httpHeaders = Maps.newHashMap();
    httpHeaders.put("a", "1.5");
    filter.filterModules(modules, httpHeaders, new Predicate<ProtocolModule>() {
      @Override
      public boolean apply(ProtocolModule input) {
        return true;
      }
    });

    assertEquals(1, modules.size());
  }

  @Test
  public void testAboveExpandable() {
    ProtocolModuleFilter filter = new ProtocolModuleFilter(new VersionComparator());
    Set<ProtocolModule> modules = Sets.newHashSet((ProtocolModule) new a2plus());

    Map<String, String> httpHeaders = Maps.newHashMap();
    httpHeaders.put("a", "4");
    filter.filterModules(modules, httpHeaders, new Predicate<ProtocolModule>() {
      @Override
      public boolean apply(ProtocolModule input) {
        return true;
      }
    });

    assertEquals(1, modules.size());
  }
}
