package org.protobee.util;

import java.util.Collection;

import org.protobee.compatability.VersionRange;
import org.protobee.compatability.VersionRangeMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionUtils {
  private static final Logger log = LoggerFactory.getLogger(VersionUtils.class);

  public static String findSmallestMax(String value, Collection<VersionRange> versions,
      VersionRangeMerger merger, VersionComparator comparator) {
    String smallestMax = null;

    if (!VersionComparator.isValidVersionString(value)) {
      log.warn("Not a valid version string: " + value);
      return null;
    }
    for (VersionRange range : versions) {
      if (comparator.compare(range.getMinVersion(), value) > 0) {
        continue;
      }
      String currMax = range.getMaxVersion();

      if (smallestMax == null) {
        if (currMax.equals(VersionRange.PLUS)) {
          smallestMax = value;
        } else {
          smallestMax = comparator.compare(currMax, value) < 0 ? currMax : value;
        }
      }
      if (!currMax.equals(VersionRange.PLUS) && comparator.compare(currMax, value) < 0) {
        smallestMax = currMax;
      }
    }
    return smallestMax;
  }
}
