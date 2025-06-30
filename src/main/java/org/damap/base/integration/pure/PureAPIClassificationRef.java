package org.damap.base.integration.pure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import lombok.Data;

/**
 * A classification reference is a generic system to mark an entity (e.g. a project participant)
 * with a certain tag.
 *
 * @see <a
 *     href="https://helpcenter.pure.elsevier.com/en_US/pure-core/classification-schemes">Classification
 *     schemes documentation</a>
 * @see <a href="https://api.elsevierpure.com/ws/api/rapidoc.html">Elsevier Pure API doc</a>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class PureAPIClassificationRef {
  /**
   * URI individually identifying the classification. This is specific to the Pure installation and
   * no generic system exists. Always starts with <code>/dk/atira/pure/keywords/</code>.
   */
  @JsonProperty(required = true)
  @NotNull String uri;

  /**
   * Optional classification description in multiple languages. The key contains the language code.
   */
  @JsonProperty HashMap<String, String> term;
}
