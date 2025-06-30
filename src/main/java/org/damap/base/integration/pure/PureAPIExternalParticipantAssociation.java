package org.damap.base.integration.pure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * An association between a {@link PureAPIProject} and an external person.
 *
 * @see <a href="https://api.elsevierpure.com/ws/api/rapidoc.html">Elsevier Pure API doc</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class PureAPIExternalParticipantAssociation extends PureAPIParticipantAssociation {
  @JsonProperty(required = true)
  @NotNull PureAPIExternalPersonRef externalPerson;
}
