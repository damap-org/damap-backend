package org.damap.base.conversion;

import jakarta.enterprise.context.RequestScoped;
import lombok.extern.jbosslog.JBossLog;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.damap.base.domain.Dataset;
import org.damap.base.domain.Repository;
import org.damap.base.enums.EContributorRole;
import org.damap.base.rest.document.dto.ExportDocumentDTO;
import org.damap.base.rest.document.dto.TitlePageDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    return ExportDocumentDTO.builder()
            .titlePageDTO(titlePageDTO)
            .build();
  }


}
