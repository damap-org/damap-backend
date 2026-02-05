package org.damap.base.integration.pure;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Request body for the {@code POST /projects/search} Pure API endpoint.
 *
 * @see <a href="https://api.elsevierpure.com/ws/api/rapidoc.html#post-/projects/search">Elsevier
 *     Pure API doc</a>
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
class PureAPIProjectsQuery {
  @JsonProperty Integer size;
  @JsonProperty Integer offset;
  @JsonProperty String searchString;
}
