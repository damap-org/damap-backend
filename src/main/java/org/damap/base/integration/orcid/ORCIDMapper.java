package org.damap.base.integration.orcid;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.damap.base.enums.EIdentifierType;
import org.damap.base.integration.orcid.models.ORCIDExpandedSearchPerson;
import org.damap.base.integration.orcid.models.ORCIDRecord;
import org.damap.base.integration.orcid.models.base.ORCIDAffiliation;
import org.damap.base.integration.orcid.models.base.ORCIDDate;
import org.damap.base.integration.orcid.models.base.ORCIDEmail;
import org.damap.base.rest.dmp.domain.ContributorDO;
import org.damap.base.rest.dmp.domain.IdentifierDO;

/** Provides static functions that map between DAMAP and ORCID objects. */
@UtilityClass
public class ORCIDMapper {
  /**
   * Maps an ORCID {@link ORCIDExpandedSearchPerson} to a {@link
   * org.damap.base.rest.dmp.domain.ContributorDO}.
   *
   * @param orcidPerson a {@link ORCIDExpandedSearchPerson} object to map from.
   * @return the {@link org.damap.base.rest.dmp.domain.ContributorDO} mapped from the ORCID person.
   */
  public static ContributorDO mapExpandedSearchPersonEntityToDO(
      ORCIDExpandedSearchPerson orcidPerson) {
    ContributorDO contributorDO = new ContributorDO();
    contributorDO.setId(null);
    contributorDO.setFirstName(orcidPerson.getGivenNames());
    contributorDO.setLastName(orcidPerson.getFamilyNames());

    String firstMail = orcidPerson.getEmails().isEmpty() ? null : orcidPerson.getEmails().get(0);
    contributorDO.setMbox(firstMail);

    String firstAffiliation =
        orcidPerson.getAffiliations().isEmpty() ? null : orcidPerson.getAffiliations().get(0);
    contributorDO.setAffiliation(firstAffiliation);

    IdentifierDO identifierContributorDO = new IdentifierDO();
    identifierContributorDO.setIdentifier(orcidPerson.getOrcidId());
    identifierContributorDO.setType(EIdentifierType.ORCID);

    contributorDO.setPersonId(identifierContributorDO);
    return contributorDO;
  }

  /**
   * Maps an {@link ORCIDRecord} to a {@link org.damap.base.rest.dmp.domain.ContributorDO}.
   *
   * @param orcidRecord a {@link ORCIDRecord} object to map from.
   * @return a {@link org.damap.base.rest.dmp.domain.ContributorDO} object with the mapped data..
   */
  public static ContributorDO mapRecordEntityToPersonDO(ORCIDRecord orcidRecord) {
    ContributorDO contributorDO = new ContributorDO();
    contributorDO.setId(null);
    contributorDO.setFirstName(orcidRecord.getPerson().getName().getGivenNames().getValue());
    contributorDO.setLastName(orcidRecord.getPerson().getName().getFamilyName().getValue());

    var emails = orcidRecord.getPerson().getEmails().getEmail();
    if (!emails.isEmpty()) {
      var primaryMail = emails.stream().filter(ORCIDEmail::isPrimary).findFirst();

      String mail =
          primaryMail.isPresent() ? primaryMail.get().getEmail() : emails.get(0).getEmail();
      contributorDO.setMbox(mail);
    }

    List<ORCIDAffiliation> affiliations = new ArrayList<>();

    for (var groupListItem :
        List.of(
            orcidRecord.getActivitiesSummary().getEducations().getAffiliationGroup(),
            orcidRecord.getActivitiesSummary().getEmployments().getAffiliationGroup())) {

      for (var group : groupListItem) {

        for (var summary : group.getSummaries()) {
          affiliations.add(summary.getSummary());
        }
      }
    }

    if (!affiliations.isEmpty()) {
      affiliations.sort(sortByCurrentStartDate);
      contributorDO.setAffiliation(
          affiliations.get(affiliations.size() - 1).getOrganization().getName());
    }

    IdentifierDO identifierContributorDO = new IdentifierDO();
    identifierContributorDO.setIdentifier(orcidRecord.getPerson().getName().getPath());
    identifierContributorDO.setType(EIdentifierType.ORCID);

    contributorDO.setPersonId(identifierContributorDO);
    return contributorDO;
  }

  private static final Comparator<? super ORCIDAffiliation> sortByCurrentStartDate =
      ((ORCIDAffiliation a, ORCIDAffiliation b) -> {
        ORCIDDate aEndDate = a.getEndDate();
        ORCIDDate bEndDate = b.getEndDate();

        // both end dates set, so we can directly compare
        if (aEndDate != null && bEndDate != null) {
          return aEndDate.getAsDate().compareTo(bEndDate.getAsDate());
        }

        // if only one is set, it means the other is current
        if (aEndDate != null || bEndDate != null) {
          if (aEndDate == null) return 1;
          return -1;
        }

        // both end dates are null. lets compare via start date
        ORCIDDate aStartDate = a.getStartDate();
        ORCIDDate bStartDate = b.getStartDate();

        // both start dates set. compare directly
        if (aStartDate != null && bStartDate != null) {
          return aStartDate.getAsDate().compareTo(bStartDate.getAsDate());
        }

        // basically nothing set. considered equal
        if (aStartDate == null && bStartDate == null) return 0;

        // one not set.
        if (aStartDate == null) return 1;
        return -1;
      });
}
