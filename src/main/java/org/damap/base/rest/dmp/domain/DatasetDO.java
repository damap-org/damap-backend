package org.damap.base.rest.dmp.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;
import org.damap.base.enums.*;

/** DatasetDO class. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasetDO {

  private Long id;

  @Size(max = 255)
  private String title;

  private List<EDataType> type = new ArrayList<>();
  private String fileFormat = "";
  private Long size;
  private String description;
  private Boolean personalData;
  private Boolean sensitiveData;
  private Boolean legalRestrictions;

  @Size(max = 255)
  private ELicense license;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date startDate;

  @Size(max = 255)
  private String referenceHash;

  private EDataAccessType dataAccess;
  private EAccessRight selectedProjectMembersAccess;
  private EAccessRight otherProjectMembersAccess;
  private EAccessRight publicAccess;
  private Boolean delete;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date dateOfDeletion;

  @Size(max = 4000)
  private String reasonForDeletion;

  private ContributorDO deletionPerson;
  private Integer retentionPeriod;
  private IdentifierDO datasetId;
  private EDataSource source;

  private List<TechnicalResourceDO> technicalResources = new ArrayList<>();
}
