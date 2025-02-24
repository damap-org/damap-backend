package org.damap.base.rest.dmp.mapper;

import java.util.HashSet;
import lombok.experimental.UtilityClass;
import org.damap.base.domain.Contributor;
import org.damap.base.domain.Identifier;
import org.damap.base.rest.dmp.domain.ContributorDO;
import org.damap.base.rest.dmp.domain.IdentifierDO;

/** ContributorDOMapper class. */
@UtilityClass
public class ContributorDOMapper {

  /**
   * mapEntityToDO.
   *
   * @param contributor a {@link org.damap.base.domain.Contributor} object
   * @param contributorDO a {@link org.damap.base.rest.dmp.domain.ContributorDO} object
   * @return a {@link org.damap.base.rest.dmp.domain.ContributorDO} object
   */
  public ContributorDO mapEntityToDO(Contributor contributor, ContributorDO contributorDO) {

    contributorDO.setId(contributor.id);
    contributorDO.setFirstName(contributor.getFirstName());
    contributorDO.setLastName(contributor.getLastName());
    contributorDO.setMbox(contributor.getMbox());
    contributorDO.setUniversityId(contributor.getUniversityId());
    contributorDO.setAffiliation(contributor.getAffiliation());

    if (contributor.getPersonIdentifier() != null) {
      IdentifierDO identifierContributorDO = new IdentifierDO();
      IdentifierDOMapper.mapEntityToDO(contributor.getPersonIdentifier(), identifierContributorDO);
      contributorDO.setPersonId(identifierContributorDO);
    }
    if (contributor.getAffiliationId() != null) {
      IdentifierDO affiliationIdentifierDO = new IdentifierDO();
      IdentifierDOMapper.mapEntityToDO(contributor.getAffiliationId(), affiliationIdentifierDO);
      contributorDO.setAffiliationId(affiliationIdentifierDO);
    }

    if (contributor.getContributorRoles() != null) {
      contributorDO.setRoles(contributor.getContributorRoles());
    } else {
      contributorDO.setRoles(new HashSet<>());
    }

    contributorDO.setContact(contributor.getContact() != null && contributor.getContact());

    return contributorDO;
  }

  /**
   * mapDOtoEntity.
   *
   * @param contributorDO a {@link org.damap.base.rest.dmp.domain.ContributorDO} object
   * @param contributor a {@link org.damap.base.domain.Contributor} object
   * @return a {@link org.damap.base.domain.Contributor} object
   */
  public Contributor mapDOtoEntity(ContributorDO contributorDO, Contributor contributor) {

    if (contributorDO.getId() != null) {
      contributor.id = contributorDO.getId();
    }
    contributor.setFirstName(contributorDO.getFirstName());
    contributor.setLastName(contributorDO.getLastName());
    contributor.setMbox(contributorDO.getMbox());
    contributor.setUniversityId(contributorDO.getUniversityId());
    contributor.setAffiliation(contributorDO.getAffiliation());

    if (contributorDO.getPersonId() != null) {
      Identifier identifierContributor =
          contributor.getPersonIdentifier() != null
              ? contributor.getPersonIdentifier()
              : new Identifier();
      IdentifierDOMapper.mapDOtoEntity(contributorDO.getPersonId(), identifierContributor);
      contributor.setPersonIdentifier(identifierContributor);
    } else {
      contributor.setPersonIdentifier(null);
    }

    if (contributorDO.getAffiliationId() != null) {
      Identifier affiliationIdentifier =
          contributor.getAffiliationId() != null
              ? contributor.getAffiliationId()
              : new Identifier();
      IdentifierDOMapper.mapDOtoEntity(contributorDO.getAffiliationId(), affiliationIdentifier);
      contributor.setAffiliationId(affiliationIdentifier);
    } else {
      contributor.setAffiliationId(null);
    }

    contributor.setContact(contributorDO.isContact());

    if (contributorDO.getRoles() != null) {
      contributor.setContributorRoles(contributorDO.getRoles());
    } else {
      contributor.setContributorRoles(new HashSet<>());
    }

    return contributor;
  }
}
