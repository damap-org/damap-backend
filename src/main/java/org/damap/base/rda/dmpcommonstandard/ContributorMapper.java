package org.damap.base.rda.dmpcommonstandard;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.damap.base.enums.EContributorRole;
import org.damap.base.enums.EIdentifierType;
import org.damap.base.rest.dmp.domain.ContributorDO;
import org.damap.base.rest.dmp.domain.IdentifierDO;

// TODO: For all Mappers: Find a way to map most of the fields automatically and only handle
// exceptions with code
// This can be done by adapting DAMAPs data model or with libraries or a combination of the tqi

/**
 * This class implements Contributor conversion from and to the RDA DMP common standard. (See <a
 * href="https://github.com/RDA-DMP-Common/common-madmp-api">github.com/RDA-DMP-Common/common-madmp-api</a>
 * )
 *
 * <p>The conversion from the common standard into DAMAP objects is best-effort since not all data
 * can be represented.
 */
public class ContributorMapper extends AbstractMapper {
  /** Initialize the mapper with default settings (strict mode true). */
  public ContributorMapper() {
    this(true);
  }

  /**
   * Initialize the mapper with an option to turn off strict mode. Only use non-strict mode when a
   * human has to review the DMP afterward, never use it for automated imports!
   *
   * @param strict When enabled, any dropped data will cause a {@link
   *     CommonStandardCompatibilityException}. When disabled, DAMAP will do its best to represent
   *     any imported data in the common format but drop data it cannot represent.
   */
  public ContributorMapper(boolean strict) {
    super(strict);
  }

  public ContributorDO convert(Contributor contributor) {
    var result = new ContributorDO();
    result.setPersonId(convertContributorID(contributor.getContributorId()));
    result.setMbox(contributor.getMbox());
    var nameParts = parseName(contributor.getName());
    result.setFirstName(nameParts[0]);
    result.setLastName(nameParts[1]);
    result.setRoles(convertRoles(contributor.getRole()));

    if (contributor.getAffiliation() != null && !contributor.getAffiliation().isEmpty()) {
      var rdaAffiliation = contributor.getAffiliation().get(0);
      result.setAffiliation(rdaAffiliation.getName());

      if (rdaAffiliation.getAffiliationId() != null) {
        var damapAffiliationId = new IdentifierDO();
        damapAffiliationId.setIdentifier(rdaAffiliation.getAffiliationId().getIdentifier());
        if (rdaAffiliation.getAffiliationId().getType() != null) {
          damapAffiliationId.setType(
              IdentifierMapper.getIdentifierDO(rdaAffiliation.getAffiliationId().getIdentifier())
                  .getType());
        } else {
          damapAffiliationId.setType(EIdentifierType.OTHER);
        }
        result.setAffiliationId(damapAffiliationId);
      }
    }
    return result;
  }

  private Set<EContributorRole> convertRoles(Set<String> roles) {
    if (roles == null) return new java.util.HashSet<>();
    return roles.stream()
        .map(this::convertRole)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }

  private EContributorRole convertRole(String role) {
    var normalizedRole = normalizeRole(role);
    for (EContributorRole e : EContributorRole.values()) {
      if (normalizeRole(e.toString()).equals(normalizedRole)
          || normalizeRole(e.name()).equals(normalizedRole)) {
        return e;
      }
    }
    if (strict) {
      throw new CommonStandardCompatibilityException(
          "cannot automatically map role " + role + " as DAMAP does not support arbitrary roles");
    }
    return null;
  }

  private String normalizeRole(String role) {
    return role.replaceAll("_", " ").trim().replaceAll("\s+", " ").toLowerCase().trim();
  }

  public Contributor convert(ContributorDO contributorDO) {
    var result = new Contributor();
    if (contributorDO.getPersonId() != null
        && contributorDO.getPersonId().getIdentifier() != null) {
      result.setContributorId(convertContributorID(contributorDO.getPersonId()));
    } else {
      result.setContributorId(new ContributorID().identifier("not-provided").type("other"));
    }
    result.setMbox(contributorDO.getMbox());
    String name = convertName(contributorDO);
    result.setName(name == null || name.isBlank() ? "Unknown Contributor" : name);
    if (contributorDO.getRoles() != null && !contributorDO.getRoles().isEmpty()) {
      result.setRole(contributorDO.getRoles().stream().map(Enum::name).collect(Collectors.toSet()));
    } else {
      result.setRole(Set.of("Other"));
    }

    if (contributorDO.getAffiliation() != null && !contributorDO.getAffiliation().isEmpty()) {
      var rdaAffiliation = new Affiliation();
      rdaAffiliation.setName(contributorDO.getAffiliation());

      var rdaAffiliationId = new AffiliationID();

      if (contributorDO.getAffiliationId() != null
          && contributorDO.getAffiliationId().getIdentifier() != null) {
        rdaAffiliationId.setIdentifier(contributorDO.getAffiliationId().getIdentifier());
        if (contributorDO.getAffiliationId().getType() != null) {
          rdaAffiliationId.setType(
              contributorDO.getAffiliationId().getType().toString().toLowerCase());
        } else {
          rdaAffiliationId.setType("other");
        }
      } else {
        rdaAffiliationId.setIdentifier("not-provided");
        rdaAffiliationId.setType("other");
      }
      rdaAffiliation.setAffiliationId(rdaAffiliationId);
      result.setAffiliation(List.of(rdaAffiliation));
    }
    return result;
  }

  public Contact convertToContact(ContributorDO contributorDO) {
    var result = new Contact();
    String name = convertName(contributorDO);
    result.setName(name == null || name.isBlank() ? "Unknown Contact" : name);
    String mbox = contributorDO.getMbox();
    result.setMbox(mbox == null || mbox.isBlank() ? "no-reply@example.com" : mbox);
    if (contributorDO.getPersonId() != null
        && contributorDO.getPersonId().getIdentifier() != null) {
      result.setContactId(convertContactID(contributorDO.getPersonId()));
    } else {
      result.setContactId(new ContactID().identifier("0").type("other"));
    }
    return result;
  }

  public ContributorDO convertToContributor(Contact contact) {
    var result = new ContributorDO();
    result.setPersonId(convertContactID(contact.getContactId()));
    result.setMbox(contact.getMbox());
    var nameParts = parseName(contact.getName());
    result.setFirstName(nameParts[0]);
    result.setLastName(nameParts[1]);
    return result;
  }

  private static String[] parseName(String name) {
    // Please note: https://www.kalzumeus.com/2010/06/17/falsehoods-programmers-believe-about-names/
    if (name == null || name.isEmpty()) {
      return new String[] {null, null};
    }
    if (name.contains(",")) {
      var parts = name.split(",", 2);
      return new String[] {parts[1].trim(), parts[0].trim()};
    }
    if (name.contains(" ")) {
      var parts = name.split(" ", 2);
      return new String[] {parts[0].trim(), parts[1].trim()};
    }
    return new String[] {name.trim(), ""};
  }

  private static String convertName(ContributorDO contributorDO) {
    var firstName = contributorDO.getFirstName();
    var lastName = contributorDO.getLastName();
    if (firstName != null && !firstName.isEmpty()) {
      if (lastName != null && !lastName.isEmpty()) {
        return firstName + " " + lastName;
      }
      return firstName;
    }
    if (lastName != null && !lastName.isEmpty()) {
      return lastName;
    }
    return "";
  }

  private ContributorID convertContributorID(IdentifierDO contributorId) {
    if (contributorId == null) return null;
    var result = new ContributorID();
    result.setIdentifier(contributorId.getIdentifier());
    // Map the DAMAP Enum to a lowercase String for RDA
    if (contributorId.getType() != null) {
      result.setType(contributorId.getType().toString().toLowerCase());
    } else {
      result.setType("other");
    }
    return result;
  }

  private IdentifierDO convertContributorID(ContributorID contributorId) {
    if (contributorId == null || contributorId.getType() == null) return null;
    var result = new IdentifierDO();
    result.setIdentifier(contributorId.getIdentifier());
    // Switch on the lowercase String from RDA to get DAMAP Enum
    result.setType(
        switch (contributorId.getType().toLowerCase()) {
          case "isni" -> EIdentifierType.ISNI;
          case "orcid" -> EIdentifierType.ORCID;
          case "openid" -> EIdentifierType.OPENID;
          default -> EIdentifierType.OTHER;
        });
    return result;
  }

  private ContactID convertContactID(IdentifierDO contactID) {
    if (contactID == null) return null;
    var result = new ContactID();
    result.setIdentifier(contactID.getIdentifier());
    if (contactID.getType() != null) {
      result.setType(contactID.getType().toString().toLowerCase());
    } else {
      result.setType("other");
    }
    return result;
  }

  private IdentifierDO convertContactID(ContactID contactID) {
    if (contactID == null || contactID.getType() == null) return null;
    var result = new IdentifierDO();
    result.setIdentifier(contactID.getIdentifier());
    result.setType(
        switch (contactID.getType().toLowerCase()) {
          case "isni" -> EIdentifierType.ISNI;
          case "orcid" -> EIdentifierType.ORCID;
          case "openid" -> EIdentifierType.OPENID;
          default -> EIdentifierType.OTHER;
        });
    return result;
  }
}
