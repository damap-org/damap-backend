package org.damap.base.integration.orcid.models.base;

import lombok.Data;

/** ORCIDValueType class. */
@Data
public class ORCIDValueType {
  String value;

  /**
   * asInt.
   *
   * @return a {@link java.lang.Integer} object
   */
  public Integer asInt() {
    Integer converted = null;
    try {
      converted = Integer.parseInt(value, 10);
    } catch (Exception e) {
      // will return null
    }

    return converted;
  }
}
