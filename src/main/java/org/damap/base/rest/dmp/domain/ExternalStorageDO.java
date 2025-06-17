package org.damap.base.rest.dmp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** ExternalStorageDO class. */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalStorageDO extends HostDO {

  @Size(max = 255)
  private String url;

  @Size(max = 255)
  private String backupFrequency;

  @Size(max = 255)
  private String storageLocation;

  @Size(max = 255)
  private String backupLocation;

  @JsonProperty("isManagedInternally")
  private Boolean isManagedInternally;
}
