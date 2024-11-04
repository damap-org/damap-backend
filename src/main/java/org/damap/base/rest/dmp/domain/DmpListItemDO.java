package org.damap.base.rest.dmp.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import lombok.Data;
import org.damap.base.enums.EFunctionRole;

/** DmpListItemDO class. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DmpListItemDO {

  private long id;
  private String title;
  private ContributorDO contact;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date created;

  @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private Date modified;

  private String description;
  private ProjectDO project;
  private EFunctionRole accessType;

  private Long versionCount;

  private String latestVersionName;
}
