package org.damap.base.enums;

import java.util.HashMap;

/** EAgreement class. */
public enum EAgreement {
  CONSORTIUM_AGREEMENT("consortium agreement"),
  DATA_PROCESSING_AGREEMENT("data processing agreement"),
  DATA_TRANSFER_AGREEMENT("data transfer agreement"),
  JOINT_CONTROL_AGREEMENT("joint control agreement"),
  CONFIDENTIALITY_AGREEMENT("confidentiality agreement"),
  OTHER("other documents");

  private final String value;

  private static final HashMap<String, EAgreement> MAP = new HashMap<>();

  EAgreement(String value) {
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
   * @return a {@link org.damap.base.enums.EAgreement} object
   */
  public static EAgreement getByValue(String value) {
    return MAP.get(value);
  }

  static {
    for (EAgreement option : EAgreement.values()) {
      MAP.put(option.getValue(), option);
    }
  }
}
