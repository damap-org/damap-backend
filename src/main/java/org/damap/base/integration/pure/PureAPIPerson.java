package org.damap.base.integration.pure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.damap.base.enums.EIdentifierType;
import org.damap.base.rest.dmp.domain.ContributorDO;
import org.damap.base.rest.dmp.domain.IdentifierDO;

/**
 * This class is a simplified view of the Person object in Pure.
 *
 * @see <a href="https://api.elsevierpure.com/ws/api/rapidoc.html">Elsevier Pure API doc</a>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class PureAPIPerson {
  @JsonProperty String uuid;

  @JsonProperty PureAPIName name;

  @JsonProperty String orcid;

  @JsonProperty String email;

  ContributorDO toContributor() {
    ContributorDO contributor = new ContributorDO();

    IdentifierDO identifier = new IdentifierDO();
    if (orcid != null && !orcid.isEmpty()) {
      identifier.setIdentifier(orcid);
      identifier.setType(EIdentifierType.ORCID);
    } else {
      identifier.setIdentifier(uuid);
      identifier.setType(EIdentifierType.OTHER);
    }
    contributor.setUniversityId(uuid);
    contributor.setPersonId(identifier);
    contributor.setFirstName(name.getFirstName());
    contributor.setLastName(name.getLastName());
    contributor.setMbox(email);
    /*
    Fields unavailable:

    contributor.setId();
    contributor.setUniversityId();
    contributor.setAffiliation();
    contributor.setContact();
    contributor.setRoles();
    contributor.setRoleInProject();
    */
    return contributor;
  }
}
