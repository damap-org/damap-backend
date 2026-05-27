package org.damap.base.integration.rda;

public class CommonStandardCompatibilityException extends RuntimeException {
  public CommonStandardCompatibilityException(String message) {
    super(
        "Cannot convert DMP to DAMAP format: "
            + message
            + " (Note: DAMAP is not fully compliant with the common standard and cannot import all common standard objects correctly.");
  }
}
