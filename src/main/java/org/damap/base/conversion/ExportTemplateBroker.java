package org.damap.base.conversion;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.damap.base.domain.ExportTemplate;
import org.damap.base.rest.dmp.service.DmpService;

/** ExportTemplateBroker class. */
@ApplicationScoped
@JBossLog
public class ExportTemplateBroker {

  private final DmpService dmpService;
  private final ExportScienceEuropeTemplate exportScienceEuropeTemplate;
  private final ExportFWFTemplate exportFWFTemplate;
  private final ExportHorizonEuropeTemplate exportHorizonEuropeTemplate;

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
      TemplateSelectorServiceImpl templateSelectorService) {
    this.dmpService = dmpService;
    this.exportScienceEuropeTemplate = exportScienceEuropeTemplate;
    this.exportFWFTemplate = exportFWFTemplate;
    this.exportHorizonEuropeTemplate = exportHorizonEuropeTemplate;
    this.templateSelectorService = templateSelectorService;
  }

  /**
   * Decides which export template to use. Currently only supports FWF and Science Europe templates.
   *
   * @param dmpId a long
   * @return a {@link org.apache.poi.xwpf.usermodel.XWPFDocument} object
   */
  public XWPFDocument exportTemplate(long dmpId, Long templateId) {
    if (templateId == null || templateId == 0) {
      return exportScienceEuropeTemplate.exportTemplate(dmpId, 1L);
    }
    ExportTemplate template = ExportTemplate.findById(templateId);

    if (template == null || !template.isActive()) {
      return null;
    }

    return switch (template.getTemplateKey()) {
      case "FWF" -> exportFWFTemplate.exportTemplate(dmpId);
      case "HORIZON_EUROPE" -> exportHorizonEuropeTemplate.exportTemplate(dmpId);
      case "SCIENCE_EUROPE" -> exportScienceEuropeTemplate.exportTemplate(dmpId, templateId);
      default -> exportScienceEuropeTemplate.exportTemplate(dmpId, templateId);
    };
  }
}
