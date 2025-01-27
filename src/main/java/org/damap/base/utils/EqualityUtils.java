package org.damap.base.utils;

import java.util.Objects;

public class EqualityUtils {
  /**
   * Checks the nullability of two objects and compares them for equality.
   *
   * @param o1 the first object
   * @param o2 the second object
   * @return false if both are null. if they are equal return true, false otherwise
   */
  public static boolean nullExclusiveEquals(Object o1, Object o2) {
    if (o1 == null && o2 == null) {
      return false;
    }
    return Objects.equals(o1, o2);
  }
}
