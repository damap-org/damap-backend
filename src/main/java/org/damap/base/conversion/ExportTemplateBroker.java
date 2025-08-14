package org.damap.base.conversion;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.damap.base.enums.ETemplateType;
import org.damap.base.rest.dmp.service.DmpService;
import org.damap.base.rest.document.dto.ExportDocumentDTO;

/** ExportTemplateBroker class. */
@ApplicationScoped
@JBossLog
public class ExportTemplateBroker {

  private final DmpService dmpService;
  private final ExportScienceEuropeTemplate exportScienceEuropeTemplate;
  private final ExportFWFTemplate exportFWFTemplate;
  private final ExportHorizonEuropeTemplate exportHorizonEuropeTemplate;
  private final ExportDTOTemplate exportDTOTemplate;

  private final TemplateSelectorServiceImpl templateSelectorService;

  @Inject
  /**
   * Constructor for ExportTemplateBroker.
   *
   * @param dmpService a {@link org.damap.base.rest.dmp.service.DmpService} object
   * @param exportScienceEuropeTemplate a {@link
   *     org.damap.base.conversion.ExportScienceEuropeTemplate} object
   * @param exportFWFTemplate a {@link org.damap.base.conversion.ExportFWFTemplate} object
   * @param exportHorizonEuropeTemplate a {@link
   *     org.damap.base.conversion.ExportHorizonEuropeTemplate} object
   * @param templateSelectorService a {@link org.damap.base.conversion.TemplateSelectorServiceImpl}
   *     object
   */
  public ExportTemplateBroker(
      DmpService dmpService,
      ExportScienceEuropeTemplate exportScienceEuropeTemplate,
      ExportFWFTemplate exportFWFTemplate,
      ExportHorizonEuropeTemplate exportHorizonEuropeTemplate,
      ExportDTOTemplate exportDTOTemplate,
      TemplateSelectorServiceImpl templateSelectorService) {
    this.dmpService = dmpService;
    this.exportScienceEuropeTemplate = exportScienceEuropeTemplate;
    this.exportFWFTemplate = exportFWFTemplate;
    this.exportHorizonEuropeTemplate = exportHorizonEuropeTemplate;
    this.templateSelectorService = templateSelectorService;
    this.exportDTOTemplate = exportDTOTemplate;
  }

  /**
   * Decides which export template to use. Currently only supports FWF and Science Europe templates.
   *
   * @param dmpId a long
   * @return a {@link org.apache.poi.xwpf.usermodel.XWPFDocument} object
   */
  public XWPFDocument exportTemplate(long dmpId) {
    return exportTemplateByType(
        dmpId, templateSelectorService.selectTemplate(dmpService.getDmpById(dmpId)));
  }

  /**
   * exportTemplateByType.
   *
   * @param dmpId a long
   * @param type a {@link org.damap.base.enums.ETemplateType} object
   * @return a {@link org.apache.poi.xwpf.usermodel.XWPFDocument} object
   */
  public XWPFDocument exportTemplateByType(long dmpId, ETemplateType type) {
    switch (type) {
      case FWF:
        return exportFWFTemplate.exportTemplate(dmpId);
      case HORIZON_EUROPE:
        return exportHorizonEuropeTemplate.exportTemplate(dmpId);
      case SCIENCE_EUROPE:
      default:
        return exportScienceEuropeTemplate.exportTemplate(dmpId);
    }
  }

  public ExportDocumentDTO exportDTO(long dmpId) {
    return exportDTOTemplate.exportDTO(dmpId);
  }
}
