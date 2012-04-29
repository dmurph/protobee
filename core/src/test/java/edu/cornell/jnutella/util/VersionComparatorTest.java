package edu.cornell.jnutella.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class VersionComparatorTest {

  @Test
  public void testMatchingLengths() {
    VersionComparator comparator = new VersionComparator();

    assertEquals(0, comparator.compare("1", "1"));
    assertEquals(0, comparator.compare("1.0", "1.0"));
    testBothWays(comparator, 1, "1.1", "1.0");
    testBothWays(comparator, 1, "0.4.1", "0.3.1");
    testBothWays(comparator, 1, "1", "0");
  }
  
  @Test
  public void testDifferentLengths() {
    VersionComparator comparator = new VersionComparator();
    
    testBothWays(comparator, 0, "1", "1.0");
    testBothWays(comparator, 0, "0.4.1", "0.4.1.0.0");
    
    testBothWays(comparator, 1, "1.1", "1");
    testBothWays(comparator, 1, "0.4.1", "0.4");
    testBothWays(comparator, -1, "0.1", "0.1.0.0.0.0.0.1");
  }
  
  @Test
  public void testValidVersionStrings() {
    VersionComparator comparator = new VersionComparator();
    
    assertTrue(comparator.isValidVersionString("0.1"));
    assertTrue(comparator.isValidVersionString("1"));
    assertTrue(comparator.isValidVersionString("10.198888.2.5"));
    assertFalse(comparator.isValidVersionString(".1"));
    assertFalse(comparator.isValidVersionString("14.1."));
  }
  
  
  private void testBothWays(VersionComparator comparator, int result, String a, String b) {
    assertEquals(result, comparator.compare(a, b));
    assertEquals(-result, comparator.compare(b, a));
  }
}
