package org.damap.base.integration.pure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import lombok.Data;

/**
 * This class represents a {@link PureAPIClassificationRef} value that also has a localized variant.
 * The localized value is optional.
 *
 * @see <a href="https://api.elsevierpure.com/ws/api/rapidoc.html">Elsevier Pure API doc</a>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class PureAPIClassifiedFormattedLocalizedValue {
  @JsonProperty Integer pureId;

  @JsonProperty HashMap<String, String> value;

  @JsonProperty(required = true)
  @NotNull PureAPIClassificationRef type;
}
