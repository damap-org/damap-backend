package org.damap.base.integration.pure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * An association between a {@link PureAPIProject} and a {@link PureAPIPerson}.
 *
 * @see <a href="https://api.elsevierpure.com/ws/api/rapidoc.html">Elsevier Pure API doc</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class PureAPIInternalParticipantAssociation extends PureAPIParticipantAssociation {
  @JsonProperty(required = true)
  @NotNull PureAPIPersonRef person;

  @JsonProperty ArrayList<PureAPIOrganizationRef> organizations;
}
