package org.damap.base.enums;

import java.util.HashMap;

/** EContributorRole class. */
public enum EContributorRole {
  DATA_COLLECTOR("Data Collector"),
  DATA_CURATOR("Data Curator"),
  DATA_MANAGER("Data Manager"),
  DISTRIBUTOR("Distributor"),
  EDITOR("Editor"),
  HOSTING_INSTITUTION("Hosting Institution"),
  PRODUCER("Producer"),
  PROJECT_LEADER("Project Leader"),
  PROJECT_MANAGER("Project Manager"),
  PROJECT_MEMBER("Project Member"),
  REGISTRATION_AGENCY("Registration Agency"),
  REGISTRATION_AUTHORITY("Registration Authority"),
  RELATED_PERSON("Related Person"),
  RESEARCHER("Researcher"),
  RESEARCH_GROUP("Research Group"),
  RIGHTS_HOLDER("Rights Holder"),
  SPONSOR("Sponsor"),
  SUPERVISOR("Supervisor"),
  WORK_PACKAGE_LEADER("Work Package Leader"),
  PRINCIPAL_INVESTIGATOR("Principal Investigator"),
  PROJECT_COORDINATOR("Project Coordinator"),
  OTHER("Other");

  private final String role;

  private static final HashMap<String, EContributorRole> MAP = new HashMap<>();

  EContributorRole(String role) {
    this.role = role;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return role;
  }

  /**
   * Getter for the field <code>role</code>.
   *
   * @return a {@link java.lang.String} object
   */
  public String getRole() {
    return this.role;
  }

  /**
   * getByRole.
   *
   * @param role a {@link java.lang.String} object
   * @return a {@link org.damap.base.enums.EContributorRole} object
   */
  public static EContributorRole getByRole(String role) {
    return MAP.get(role);
  }

  static {
    for (EContributorRole role : EContributorRole.values()) {
      MAP.put(role.getRole(), role);
    }
  }
}
