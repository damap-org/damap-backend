package org.damap.base.integration.pure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import lombok.Data;

/**
 * A standard Pure paginated response.
 *
 * @param <T> Item being paginated.
 * @see <a href="https://api.elsevierpure.com/ws/api/rapidoc.html">Elsevier Pure API doc</a>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class PureAPIPaginatedResponse<T> {
  @JsonProperty long count;

  @JsonProperty PureAPIPageInformation pageInformation;

  @JsonProperty ArrayList<T> items;
}
