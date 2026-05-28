package org.damap.base.rda.dmpcommonstandard;

abstract class AbstractMapper {
  /**
   * Tolerate compatibility failures with the common standard and drop non-standard data. This flag
   * should never be used when importing DMP data, only for integrations.
   */
  protected final boolean strict;

  AbstractMapper(boolean strict) {
    this.strict = strict;
  }
}
