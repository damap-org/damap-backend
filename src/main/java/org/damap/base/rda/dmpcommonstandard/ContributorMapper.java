package org.damap.base.rda.dmpcommonstandard;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.damap.base.enums.EContributorRole;
import org.damap.base.enums.EIdentifierType;
import org.damap.base.rest.dmp.domain.ContributorDO;
import org.damap.base.rest.dmp.domain.IdentifierDO;

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
    return result;
  }

  private Set<EContributorRole> convertRoles(Set<String> roles) {
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
    result.setContributorId(convertContributorID(contributorDO.getPersonId()));
    result.setMbox(contributorDO.getMbox());
    result.setName(convertName(contributorDO));
    result.setRole(contributorDO.getRoles().stream().map(Enum::name).collect(Collectors.toSet()));
    return result;
  }

  public Contact convertToContact(ContributorDO contributorDO) {
    var result = new Contact();
    result.setContactId(convertContactID(contributorDO.getPersonId()));
    result.setMbox(contributorDO.getMbox());
    result.setName(convertName(contributorDO));
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
    if (contributorId == null) {
      return null;
    }
    return switch (contributorId.getType()) {
      case ISNI -> {
        var result = new ContributorID();
        result.setType(ContributorIDType.ISNI);
        result.setIdentifier(contributorId.getIdentifier());
        yield result;
      }
      case ORCID -> {
        var result = new ContributorID();
        result.setType(ContributorIDType.ORCID);
        result.setIdentifier(contributorId.getIdentifier());
        yield result;
      }
      case OPENID -> {
        var result = new ContributorID();
        result.setType(ContributorIDType.OPENID);
        result.setIdentifier(contributorId.getIdentifier());
        yield result;
      }
      default -> {
        var result = new ContributorID();
        result.setType(ContributorIDType.OTHER);
        result.setIdentifier(contributorId.getIdentifier());
        yield result;
      }
    };
  }

  private IdentifierDO convertContributorID(ContributorID contributorId) {
    if (contributorId == null) {
      return null;
    }
    return switch (contributorId.getType()) {
      case ISNI -> {
        var result = new IdentifierDO();
        result.setType(EIdentifierType.ISNI);
        result.setIdentifier(contributorId.getIdentifier());
        yield result;
      }
      case ORCID -> {
        var result = new IdentifierDO();
        result.setType(EIdentifierType.ORCID);
        result.setIdentifier(contributorId.getIdentifier());
        yield result;
      }
      case OPENID -> {
        var result = new IdentifierDO();
        result.setType(EIdentifierType.OPENID);
        result.setIdentifier(contributorId.getIdentifier());
        yield result;
      }
      case OTHER -> {
        var result = new IdentifierDO();
        result.setType(EIdentifierType.OTHER);
        result.setIdentifier(contributorId.getIdentifier());
        yield result;
      }
    };
  }

  private ContactID convertContactID(IdentifierDO contactID) {
    if (contactID == null) {
      return null;
    }
    return switch (contactID.getType()) {
      case ISNI -> {
        var result = new ContactID();
        result.setType(ContactIDType.ISNI);
        result.setIdentifier(contactID.getIdentifier());
        yield result;
      }
      case ORCID -> {
        var result = new ContactID();
        result.setType(ContactIDType.ORCID);
        result.setIdentifier(contactID.getIdentifier());
        yield result;
      }
      case OPENID -> {
        var result = new ContactID();
        result.setType(ContactIDType.OPENID);
        result.setIdentifier(contactID.getIdentifier());
        yield result;
      }
      default -> {
        var result = new ContactID();
        result.setType(ContactIDType.OTHER);
        result.setIdentifier(contactID.getIdentifier());
        yield result;
      }
    };
  }

  private IdentifierDO convertContactID(ContactID contactID) {
    if (contactID == null) {
      return null;
    }
    return switch (contactID.getType()) {
      case ISNI -> {
        var result = new IdentifierDO();
        result.setType(EIdentifierType.ISNI);
        result.setIdentifier(contactID.getIdentifier());
        yield result;
      }
      case ORCID -> {
        var result = new IdentifierDO();
        result.setType(EIdentifierType.ORCID);
        result.setIdentifier(contactID.getIdentifier());
        yield result;
      }
      case OPENID -> {
        var result = new IdentifierDO();
        result.setType(EIdentifierType.OPENID);
        result.setIdentifier(contactID.getIdentifier());
        yield result;
      }
      case OTHER -> {
        var result = new IdentifierDO();
        result.setType(EIdentifierType.OTHER);
        result.setIdentifier(contactID.getIdentifier());
        yield result;
      }
    };
  }
}
