package org.protobee.compatability;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;
import org.protobee.util.VersionComparator;

import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;

public class VersionMergerTest {


  private static class Module extends AbstractModule {

    @Override
    protected void configure() {
      bind(VersionRangeMerger.class).in(Singleton.class);
      bind(VersionComparator.class).in(Singleton.class);
      bind(VersionRangeMerger.class).in(Singleton.class);
    }

  }

  @Test
  public void testUnionNoHole() {
    Set<VersionRange> ranges =
        Sets.newHashSet(new VersionRange("0.1", "1.0"), new VersionRange("0.5", "4.2.1"),
            new VersionRange("2", "3"));

    Injector inj = Guice.createInjector(new Module());

    VersionRangeMerger merger = inj.getInstance(VersionRangeMerger.class);

    Set<VersionRange> result = Sets.newHashSet(new VersionRange("0.1", "4.2.1"));
    assertEquals(result, merger.union(ranges));
  }

  @Test
  public void testUnionWithHole() {
    Set<VersionRange> ranges =
        Sets.newHashSet(new VersionRange("0.1", "1.0"), new VersionRange("1.5", "4.2.1"),
            new VersionRange("1.2", "3"));

    Injector inj = Guice.createInjector(new Module());

    VersionRangeMerger merger = inj.getInstance(VersionRangeMerger.class);


    Set<VersionRange> result =
        Sets.newHashSet(new VersionRange("0.1", "1.0"), new VersionRange("1.2", "4.2.1"));
    assertEquals(result, merger.union(ranges));
  }

  @Test
  public void testIntersection() {
    Set<VersionRange> ranges =
        Sets.newHashSet(new VersionRange("0.1", "1.0"), new VersionRange("0.2", "1.0"),
            new VersionRange("0.1", "0.9"), new VersionRange("0.3", "0.4"));

    Injector inj = Guice.createInjector(new Module());

    VersionRangeMerger merger = inj.getInstance(VersionRangeMerger.class);

    assertEquals(new VersionRange("0.3", "0.4"), merger.intersection(ranges));
  }

  @Test
  public void testImpossibleIntersection() {
    Set<VersionRange> ranges =
        Sets.newHashSet(new VersionRange("0.1", "1.0"), new VersionRange("0.2", "1.0"),
            new VersionRange("0.1", "0.9"), new VersionRange("0.3", "0.4"), new VersionRange(
                "1.0.1", "2"));

    Injector inj = Guice.createInjector(new Module());

    VersionRangeMerger merger = inj.getInstance(VersionRangeMerger.class);

    assertNull(merger.intersection(ranges));
  }

  @Test
  public void testSubtractNoHole() {
    Set<VersionRange> ranges =
        Sets.newHashSet(new VersionRange("0.1", "1.0"), new VersionRange("1.4.3", "2"));

    VersionRange subtractFrom = new VersionRange("0.5", "1.5");

    Injector inj = Guice.createInjector(new Module());

    VersionRangeMerger merger = inj.getInstance(VersionRangeMerger.class);

    Set<VersionRange> result = Sets.newHashSet(new VersionRange("1.0", "1.4.3"));
    assertEquals(result, merger.subtract(subtractFrom, ranges));
  }

  @Test
  public void testSubtractWithHole() {
    Set<VersionRange> ranges =
        Sets.newHashSet(new VersionRange("0.1", "1.0"), new VersionRange("1.4.3", "2"),
            new VersionRange("1.2", "1.3"));

    VersionRange subtractFrom = new VersionRange("0.5", "1.5");

    Injector inj = Guice.createInjector(new Module());

    VersionRangeMerger merger = inj.getInstance(VersionRangeMerger.class);

    Set<VersionRange> result =
        Sets.newHashSet(new VersionRange("1.0", "1.2"), new VersionRange("1.3", "1.4.3"));
    assertEquals(result, merger.subtract(subtractFrom, ranges));
  }
  
  @Test
  public void testSubtractWithHole2() {
    Set<VersionRange> ranges =
        Sets.newHashSet(new VersionRange("0.1", "1.0"));

    VersionRange subtractFrom = new VersionRange("0.1", "1.0");

    Injector inj = Guice.createInjector(new Module());

    VersionRangeMerger merger = inj.getInstance(VersionRangeMerger.class);

    Set<VersionRange> result =
        Sets.newHashSet(new VersionRange("0.1", "0.1"), new VersionRange("1.0", "1.0"));
    assertEquals(result, merger.subtract(subtractFrom, ranges));
  }
}
