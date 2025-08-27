package org.damap.base.integration.pure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Abstract parent class of a project participant.
 *
 * @see <a href="https://api.elsevierpure.com/ws/api/rapidoc.html">Elsevier Pure API doc</a>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "typeDiscriminator")
@JsonSubTypes({
  @JsonSubTypes.Type(
      value = PureAPIInternalParticipantAssociation.class,
      name = "InternalParticipantAssociation"),
  @JsonSubTypes.Type(
      value = PureAPIExternalParticipantAssociation.class,
      name = "ExternalParticipantAssociation")
})
abstract class PureAPIParticipantAssociation {
  @JsonProperty long pureId;

  @JsonProperty PureAPIName name;

  @JsonProperty(required = true)
  @NotNull PureAPIClassificationRef role;
}
