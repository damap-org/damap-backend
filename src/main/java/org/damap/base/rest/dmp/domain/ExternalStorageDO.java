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

  // This field indicates whether this specific external storage is managed internally or not.
  // It is possible, that a storage is not indicated as internal storage (by the admin in the Admin
  // UI),
  // but is still managed internally. In this case, this field should be set to true.
  @JsonProperty("isManagedInternally")
  private Boolean isManagedInternally;
}
