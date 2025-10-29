package org.damap.base.rest.document.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StorageInformationDTO {

  private List<StorageLocation> storageLocations;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class StorageLocation {
    private String title;
    private StorageType type;
    private String description;
    private Boolean isManagedInternally;
    private String externalStorageInfo;

    private List<DatasetSummary> datasets;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DatasetSummary {
    private String datasetId;
    private String title;
  }

  public enum StorageType {
    INTERNAL,
    EXTERNAL
  }
}