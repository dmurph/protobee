package org.protobee.util;

import java.util.Comparator;


/**
 * Preconditions: strings have only numbers and dots, no space, and start + end with numbers (no
 * leading/trailing dots).
 * 
 * @author Daniel
 * 
 */
public class VersionComparator implements Comparator<String> {

  public boolean isValidVersionString(String str) {
    return str.matches("[0-9]+(\\.[0-9]+)*");
  }
  
  @Override
  public int compare(String o1, String o2) {
    if (o1 == null) {
      if (o2 == null) {
        return 0;
      }
      return 1;
    }
    if (o2 == null) {
      return -1;
    }

    String[] nums1 = o1.split("\\.");
    String[] nums2 = o2.split("\\.");

    int index = 0;
    while (true) {
      if (index >= nums1.length) {
        if (index >= nums2.length) {
          return 0;
        }
        while (index < nums2.length) {
          int num = Integer.parseInt(nums2[index]);
          if (num != 0) {
            return -1;
          }
          index++;
        }
        return 0;
      }
      if (index >= nums2.length) {
        while (index < nums1.length) {
          int num = Integer.parseInt(nums1[index]);
          if (num != 0) {
            return 1;
          }
          index++;
        }
        return 0;
      }
      int num1 = Integer.parseInt(nums1[index]);
      int num2 = Integer.parseInt(nums2[index]);
      if (num1 == num2) {
        index++;
        continue;
      }
      if (num1 < num2) {
        return -1;
      }
      if (num1 > num2) {
        return 1;
      }
    }
  }
}
