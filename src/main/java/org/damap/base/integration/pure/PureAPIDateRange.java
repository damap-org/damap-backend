package org.damap.base.integration.pure;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import lombok.Data;

/**
 * A range of dates with nullable values.
 *
 * @see <a href="https://api.elsevierpure.com/ws/api/rapidoc.html">Elsevier Pure API doc</a>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class PureAPIDateRange {
  @JsonProperty
  @JsonFormat(pattern = "yyyy-MM-dd")
  Date startDate;

  @JsonProperty
  @JsonFormat(pattern = "yyyy-MM-dd")
  Date endDate;
}
