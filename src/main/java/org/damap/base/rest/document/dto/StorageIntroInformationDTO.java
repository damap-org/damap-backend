package org.damap.base.rest.document.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class StorageIntroInformationDTO {

  private String coordinatorFullName;

  private Boolean usesExternalStorage;

  private Boolean isManagedInternally;
}
