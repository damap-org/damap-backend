package org.damap.base.rest.document.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExportDocumentDTO {

    private TitlePageDTO titlePageDTO;

    private ContributorInformationDTO contributorInformationDTO;

    private DatasetInformationDTO datasetInformationDTO;

    private StorageIntroInformationDTO storageIntroInformationDTO;

    private StorageInformationDTO storageInformationDTO;

    private DataQualityDTO dataQualityDTO;

    private SensitiveDataInformationDTO sensitiveDataInformationDTO;

    private LegalEthicalInformationDTO legalEthicalInformationDTO;

    private RepoInfoAndToolsInformationDTO repoInfoAndToolsInformationDTO;

    private CostInformationDTO costInformationDTO;

    private WorkPackageLeadersInformationDTO workPackageLeadersInformationDTO;
}
