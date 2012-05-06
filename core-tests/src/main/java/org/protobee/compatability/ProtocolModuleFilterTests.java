package org.protobee.compatability;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.protobee.compatability.CompatabilityHeader;
import org.protobee.compatability.Headers;
import org.protobee.modules.ProtocolModule;
import org.protobee.session.SessionProtocolModules;
import org.protobee.session.handshake.ProtocolModuleFilter;
import org.protobee.util.VersionComparator;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;


public class ProtocolModuleFilterTests {

  @Headers(required = {@CompatabilityHeader(name = "a", minVersion = "1", maxVersion = "2")}, requested = {})
  private static class a12 extends ProtocolModule {}

  @Headers(required = {@CompatabilityHeader(name = "a", minVersion = "2", maxVersion = "+")}, requested = {})
  private static class a2plus extends ProtocolModule {}
  
  @Headers(required = {}, silentExcluding={@CompatabilityHeader(name = "a", minVersion = "0.1", maxVersion = "+")})
  private static class noa extends ProtocolModule {}

  @Test
  public void testLowerVersion() {
    Set<ProtocolModule> modules = Sets.newHashSet((ProtocolModule) new a12());
    SessionProtocolModules pmodules = new SessionProtocolModules(modules);
    EventBus bus = mock(EventBus.class);
    ProtocolModuleFilter filter = new ProtocolModuleFilter(new VersionComparator(), pmodules, bus);

    Map<String, String> httpHeaders = Maps.newHashMap();
    httpHeaders.put("a", "0");
    filter.filterModules(httpHeaders);

    assertEquals(0, pmodules.getMutableModules().size());
  }

  @Test
  public void testRequiredNotPresent() {
    Set<ProtocolModule> modules = Sets.newHashSet((ProtocolModule) new a12());
    SessionProtocolModules pmodules = new SessionProtocolModules(modules);
    EventBus bus = mock(EventBus.class);
    ProtocolModuleFilter filter = new ProtocolModuleFilter(new VersionComparator(), pmodules, bus);

    Map<String, String> httpHeaders = Maps.newHashMap();
    filter.filterModules(httpHeaders);

    assertEquals(0, pmodules.getMutableModules().size());
  }

  @Test
  public void testToHigh() {
    Set<ProtocolModule> modules = Sets.newHashSet((ProtocolModule) new a12());
    SessionProtocolModules pmodules = new SessionProtocolModules(modules);
    EventBus bus = mock(EventBus.class);
    ProtocolModuleFilter filter = new ProtocolModuleFilter(new VersionComparator(), pmodules, bus);

    Map<String, String> httpHeaders = Maps.newHashMap();
    httpHeaders.put("a", "3");
    filter.filterModules(httpHeaders);

    assertEquals(0, pmodules.getMutableModules().size());
  }


  @Test
  public void testFits() {
    Set<ProtocolModule> modules = Sets.newHashSet((ProtocolModule) new a12());
    SessionProtocolModules pmodules = new SessionProtocolModules(modules);
    EventBus bus = mock(EventBus.class);
    ProtocolModuleFilter filter = new ProtocolModuleFilter(new VersionComparator(), pmodules, bus);

    Map<String, String> httpHeaders = Maps.newHashMap();
    httpHeaders.put("a", "1.5");
    filter.filterModules(httpHeaders);

    assertEquals(1, pmodules.getMutableModules().size());
  }

  @Test
  public void testAboveExpandable() {
    Set<ProtocolModule> modules = Sets.newHashSet((ProtocolModule) new a2plus());
    SessionProtocolModules pmodules = new SessionProtocolModules(modules);
    EventBus bus = mock(EventBus.class);
    ProtocolModuleFilter filter = new ProtocolModuleFilter(new VersionComparator(), pmodules, bus);

    Map<String, String> httpHeaders = Maps.newHashMap();
    httpHeaders.put("a", "4");
    filter.filterModules(httpHeaders);

    assertEquals(1, pmodules.getMutableModules().size());
  }
  
  @Test
  public void testRemoveSilently() {
    Set<ProtocolModule> modules = Sets.<ProtocolModule>newHashSet(new a2plus(), new noa());
    SessionProtocolModules pmodules = new SessionProtocolModules(modules);
    EventBus bus = mock(EventBus.class);
    ProtocolModuleFilter filter = new ProtocolModuleFilter(new VersionComparator(), pmodules, bus);

    Map<String, String> httpHeaders = Maps.newHashMap();
    httpHeaders.put("a", "4");
    filter.filterModules(httpHeaders);

    assertEquals(1, pmodules.getMutableModules().size());
  }
}
