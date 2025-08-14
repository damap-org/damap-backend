package org.damap.base.rest.document.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatasetInformationDTO {

    private String datageneration;
    private String documentation;
    private String targetaudience;
    private String reuseddatadescription;
    private String produceddatadescription;
    private String datasetTechnicalResources;
    private String datamananager;
    private String datamanagerInfo;

}
