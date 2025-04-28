package org.damap.base.enums;

import java.util.HashMap;

/** EAccessRight class. */
public enumf EAccessRight {
  READ("reading only"),
  WRITE("writing"),
  NONE("no access");

  private final String value;

  private static final HashMap<String, EAccessRight> MAP = new HashMap<>();

  EAccessRight(String value) {
    this.value = value;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return value;
  }

  /**
   * Getter for the field <code>value</code>.
   *
   * @return a {@link java.lang.String} object
   */
  public String getValue() {
    return this.value;
  }

  /**
   * getByValue.
   *
   * @param value a {@link java.lang.String} object
   * @return a {@link org.damap.base.enums.EAccessRight} object
   */
  public static EAccessRight getByValue(String value) {
    return MAP.get(value);
  }

  static {
    for (EAccessRight option : EAccessRight.values()) {
      MAP.put(option.getValue(), option);
    }
  }
}
