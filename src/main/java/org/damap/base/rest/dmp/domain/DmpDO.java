package org.damap.base.rest.dmp.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import org.damap.base.enums.*;

/** DmpDO class. */
@Valid
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DmpDO {

  private Long id;

  @Size(max = 255)
  private String title;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date created;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date modified;

  private String description;
  private ProjectDO project;
  private EDataKind dataKind;
  private EDataKind reusedDataKind;
  private List<ContributorDO> contributors = new ArrayList<>();

  @Size(max = 4000)
  private String noDataExplanation;

  @Size(max = 4000)
  private String metadata;

  @Size(max = 4000)
  private String dataGeneration;

  @Size(max = 4000)
  private String structure;

  private List<EDataQualityType> dataQuality = new ArrayList<>();

  @Size(max = 4000)
  private String otherDataQuality;

  @Size(max = 4000)
  private String targetAudience;

  @Size(max = 4000)
  private String tools;

  @Size(max = 4000)
  private String restrictedDataAccess;

  private Boolean personalData;
  private Boolean personalDataCris;
  private List<EComplianceType> personalDataCompliance = new ArrayList<>();

  @Size(max = 4000)
  private String otherPersonalDataCompliance;

  private Boolean sensitiveData;
  private Boolean sensitiveDataCris;
  private List<ESecurityMeasure> sensitiveDataSecurity = new ArrayList<>();

  @Size(max = 4000)
  private String otherDataSecurityMeasures;

  @Size(max = 4000)
  private String sensitiveDataAccess;

  private Boolean legalRestrictions;
  private Boolean legalRestrictionsCris;
  private List<EAgreement> legalRestrictionsDocuments = new ArrayList<>();

  @Size(max = 4000)
  private String otherLegalRestrictionsDocument;

  @Size(max = 4000)
  private String legalRestrictionsComment;

  @Size(max = 4000)
  private String dataRightsAndAccessControl;

  private Boolean humanParticipants;
  private Boolean humanParticipantsCris;
  private Boolean ethicalIssuesExist;
  private Boolean ethicalIssuesExistCris;
  private Boolean committeeReviewed;
  private Boolean committeeReviewedCris;

  @Size(max = 255)
  private String ethicalIssuesReport;

  private List<DatasetDO> datasets = new ArrayList<>();
  private List<RepositoryDO> repositories = new ArrayList<>();
  private List<StorageDO> storage = new ArrayList<>();
  private List<ExternalStorageDO> externalStorage = new ArrayList<>();

  @Size(max = 4000)
  private String externalStorageInfo;

  @Size(max = 4000)
  private String restrictedAccessInfo;

  @Size(max = 4000)
  private String closedAccessInfo;

  private Boolean costsExist;
  private Boolean costsExistCris;
  private List<CostDO> costs = new ArrayList<>();

  @Size(max = 4000)
  private String documentation;

  /**
   * getContact.
   *
   * @return a {@link org.damap.base.rest.dmp.domain.ContributorDO} object
   */
  public ContributorDO getContact() {
    Optional<ContributorDO> contact =
        contributors.stream().filter(ContributorDO::isContact).findFirst();
    return contact.orElse(null);
  }
}
