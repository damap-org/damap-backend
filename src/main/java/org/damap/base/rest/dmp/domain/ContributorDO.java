package org.damap.base.rest.dmp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import org.damap.base.enums.EContributorRole;

/** Damap compatible representation of persons */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContributorDO {

  private Long id;

  @Size(max = 255)
  private String universityId;

  private IdentifierDO personId;

  @Size(max = 255)
  private String firstName;

  @Size(max = 255)
  private String lastName;

  @Size(max = 255)
  private String mbox;

  @Size(max = 255)
  private String affiliation;

  private IdentifierDO affiliationId;
  private boolean contact = false;

  private Set<EContributorRole> roles = new HashSet<>();
  // not stored in DB, therefore no size contraint
  private String roleInProject;
}
