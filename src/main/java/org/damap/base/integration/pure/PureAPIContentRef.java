package org.damap.base.integration.pure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * A reference to a different entity.
 *
 * @see <a href="https://api.elsevierpure.com/ws/api/rapidoc.html">Elsevier Pure API doc</a>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "systemName")
@JsonSubTypes({
  @JsonSubTypes.Type(value = PureAPIPersonRef.class, name = "Person"),
  @JsonSubTypes.Type(value = PureAPIExternalPersonRef.class, name = "ExternalPerson"),
  @JsonSubTypes.Type(value = PureAPIOrganizationRef.class, name = "Organization"),
  @JsonSubTypes.Type(value = PureAPIExternalOrganizationRef.class, name = "ExternalOrganization")
})
abstract class PureAPIContentRef {
  @JsonProperty(required = true)
  @NotNull String uuid;
}
