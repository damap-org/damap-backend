package org.damap.base.conversion;

import jakarta.enterprise.context.RequestScoped;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.rest.document.dto.*;

/** ExportHorizonEuropeTemplate class. */
@RequestScoped
@JBossLog
public class ExportDTOTemplate extends AbstractTemplateExportScienceEuropeComponents {
  /**
   * exportTemplate.
   *
   * @param dmpId a long
   * @return a {@link ExportDocumentDTO} object
   */
  public ExportDocumentDTO exportDTO(long dmpId) {
    log.info("Exporting DTO for DMP with ID: " + dmpId);
    exportSetup(dmpId);

    TitlePageDTO titlePageDTO = this.titlePageDTO();
    ContributorInformationDTO contributorInformationDTO = this.contributorInformationDTO();
    DatasetInformationDTO datasetInformationDTO = this.datasetInformationDTO();
    StorageIntroInformationDTO storageIntroInformationDTO = this.storageIntroInformationDTO();
    StorageInformationDTO storageInformationDTO = this.storageInformationDTO();

    CostInformationDTO costInformationDTO = this.costInformationDTO();

    return ExportDocumentDTO.builder()
        .titlePageDTO(titlePageDTO)
        .contributorInformationDTO(contributorInformationDTO)
        .datasetInformationDTO(datasetInformationDTO)
        .storageIntroInformationDTO(storageIntroInformationDTO)
        .storageInformationDTO(storageInformationDTO)
        .costInformationDTO(costInformationDTO)
        .build();
  }
}
