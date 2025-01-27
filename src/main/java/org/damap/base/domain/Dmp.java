package org.damap.base.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import org.damap.base.enums.*;
import org.hibernate.Length;
import org.hibernate.envers.Audited;

/** Dmp class. */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Audited
@Table
public class Dmp extends PanacheEntity {

  @Version
  @Setter(AccessLevel.NONE)
  private long version;

  @Temporal(TemporalType.TIMESTAMP)
  private Date created;

  @Temporal(TemporalType.TIMESTAMP)
  private Date modified;

  private String title;

  @Column(length = Length.LONG32)
  private String description;

  @ManyToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "project_id")
  private Project project;

  @Enumerated(EnumType.STRING)
  @Column(name = "data_kind")
  private EDataKind dataKind;

  @Enumerated(EnumType.STRING)
  @Column(name = "reused_data_kind")
  private EDataKind reusedDataKind;

  @Column(name = "no_data_explanation")
  private String noDataExplanation;

  private String metadata;

  @Column(name = "data_generation")
  private String dataGeneration;

  private String structure;

  @ElementCollection(targetClass = EDataQualityType.class, fetch = FetchType.LAZY)
  @CollectionTable(name = "data_quality")
  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  private List<EDataQualityType> dataQuality;

  @Column(name = "other_data_quality")
  private String otherDataQuality;

  @Column(name = "target_audience")
  private String targetAudience;

  private String tools;

  @Column(name = "restricted_data_access")
  private String restrictedDataAccess;

  @Column(name = "personal_data")
  private Boolean personalData;

  @Column(name = "personal_data_cris")
  private Boolean personalDataCris;

  @ElementCollection(targetClass = EComplianceType.class, fetch = FetchType.LAZY)
  @CollectionTable(name = "personal_data_compliance")
  @Column(name = "compliance_type")
  @Enumerated(EnumType.STRING)
  private List<EComplianceType> personalDataCompliance;

  @Column(name = "other_personal_data_compliance")
  private String otherPersonalDataCompliance;

  @Column(name = "sensitive_data")
  private Boolean sensitiveData;

  @Column(name = "sensitive_data_cris")
  private Boolean sensitiveDataCris;

  @ElementCollection(targetClass = ESecurityMeasure.class, fetch = FetchType.LAZY)
  @CollectionTable(name = "sensitive_data_security")
  @Column(name = "security_measure")
  @Enumerated(EnumType.STRING)
  private List<ESecurityMeasure> sensitiveDataSecurity;

  @Column(name = "other_data_sec_measures")
  private String otherDataSecurityMeasures;

  @Column(name = "sensitive_data_access")
  private String sensitiveDataAccess;

  @Column(name = "legal_restrictions")
  private Boolean legalRestrictions;

  @Column(name = "legal_restrictions_cris")
  private Boolean legalRestrictionsCris;

  @ElementCollection(targetClass = EAgreement.class, fetch = FetchType.LAZY)
  @CollectionTable(name = "legal_restr_documents")
  @Column(name = "agreement")
  @Enumerated(EnumType.STRING)
  private List<EAgreement> legalRestrictionsDocuments;

  @Column(name = "other_legal_r_documents")
  private String otherLegalRestrictionsDocument;

  @Column(name = "legal_restrictions_comment")
  private String legalRestrictionsComment;

  @Column(name = "data_rights_access_control")
  private String dataRightsAndAccessControl;

  @Column(name = "human_participants")
  private Boolean humanParticipants;

  @Column(name = "human_participants_cris")
  private Boolean humanParticipantsCris;

  @Column(name = "ethical_issues_exist")
  private Boolean ethicalIssuesExist;

  @Column(name = "ethical_issues_exist_cris")
  private Boolean ethicalIssuesExistCris;

  @Column(name = "committee_reviewed")
  private Boolean committeeReviewed;

  @Column(name = "committee_reviewed_cris")
  private Boolean committeeReviewedCris;

  @OneToMany(
      mappedBy = "dmp",
      cascade = {CascadeType.ALL},
      orphanRemoval = true,
      fetch = FetchType.EAGER)
  @OrderBy("id")
  private List<Contributor> contributorList = new ArrayList<>();

  @OneToMany(
      mappedBy = "dmp",
      cascade = {CascadeType.ALL},
      orphanRemoval = true,
      fetch = FetchType.EAGER)
  @OrderBy("id")
  private List<Dataset> datasetList = new ArrayList<>();

  @OneToMany(
      mappedBy = "dmp",
      cascade = {CascadeType.ALL},
      orphanRemoval = true,
      fetch = FetchType.EAGER)
  private List<Host> hostList = new ArrayList<>();

  @Column(name = "external_storage_info")
  private String externalStorageInfo;

  @Column(name = "restricted_access_info")
  private String restrictedAccessInfo;

  @Column(name = "closed_access_info")
  private String closedAccessInfo;

  @Column(name = "costs_exist")
  private Boolean costsExist;

  @Column(name = "costs_exist_cris")
  private Boolean costsExistCris;

  @OneToMany(
      mappedBy = "dmp",
      cascade = {CascadeType.ALL},
      orphanRemoval = true,
      fetch = FetchType.EAGER)
  @OrderBy("id")
  private List<Cost> costs = new ArrayList<>();

  private String documentation;

  /**
   * getContact.
   *
   * @return a {@link org.damap.base.domain.Contributor} object
   */
  public Contributor getContact() {
    Optional<Contributor> contact =
        contributorList.stream()
            .filter(contributor -> contributor.getContact() != null)
            .filter(Contributor::getContact)
            .findFirst();
    return contact.orElse(null);
  }

  /**
   * Return the value of the chain getProject().getUniversityId();
   *
   * @return the value of the chain getProject().getUniversityId() or null if any part of the chain
   *     is null.
   */
  public String getProjectUniversityId() {
    if (this.getProject() == null || this.getProject().getUniversityId() == null) {
      return null;
    }
    return this.getProject().getUniversityId();
  }
}
