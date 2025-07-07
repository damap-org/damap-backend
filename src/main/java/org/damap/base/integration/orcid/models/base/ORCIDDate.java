package org.damap.base.integration.orcid.models.base;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Calendar;
import java.util.Date;
import lombok.Data;

/** ORCIDDate class. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ORCIDDate {
  @JsonProperty ORCIDValueType year;

  @JsonProperty ORCIDValueType month;

  @JsonProperty ORCIDValueType day;

  /**
   * getAsDate.
   *
   * @return a {@link java.util.Date} object
   */
  public Date getAsDate() {
    Calendar c = Calendar.getInstance();
    c.set(Calendar.YEAR, year == null ? 0 : year.asInt());
    c.set(Calendar.MONTH, month == null ? 0 : month.asInt() - 1);
    c.set(Calendar.DATE, day == null ? 1 : day.asInt());
    return c.getTime();
  }
}
