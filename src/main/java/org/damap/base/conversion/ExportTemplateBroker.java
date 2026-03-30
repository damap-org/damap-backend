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

  private final ExportScienceEuropeTemplate exportScienceEuropeTemplate;
  private final ExportFWFTemplate exportFWFTemplate;
  private final ExportHorizonEuropeTemplate exportHorizonEuropeTemplate;

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
      ExportScienceEuropeTemplate exportScienceEuropeTemplate,
      ExportFWFTemplate exportFWFTemplate,
      ExportHorizonEuropeTemplate exportHorizonEuropeTemplate) {
    this.exportScienceEuropeTemplate = exportScienceEuropeTemplate;
    this.exportFWFTemplate = exportFWFTemplate;
    this.exportHorizonEuropeTemplate = exportHorizonEuropeTemplate;
  }

  @Inject DmpService dmpService;
  @Inject TemplateSelectorService templateSelectorService;

  /**
   * Decides which export template to use. Supports standard FWF, Horizon Europe, Science Europe
   * templates, as well as custom uploaded templates.
   *
   * @param dmpId the unique identifier of the DMP
   * @param templateId the unique identifier of the export template to be used (can be null for
   *     default)
   * @return a {@link org.apache.poi.xwpf.usermodel.XWPFDocument} object
   */
  public XWPFDocument exportTemplate(long dmpId, Long templateId) {
    ExportTemplate template;
    if (templateId == null || templateId == 0) {
      String templateCategory =
          templateSelectorService.selectTemplate(dmpService.getDmpById(dmpId));
      template =
          ExportTemplate.find("templateCategory = ?1 and active = true", templateCategory)
              .firstResult();
      if (template == null) {
        template = ExportTemplate.find("active = true").firstResult();
      }
    } else {
      template = ExportTemplate.findById(templateId);
    }

    if (template == null || !template.isActive()) {
      return null;
    }
    return switch (template.getTemplateCategory()) {
      case "FWF" -> exportFWFTemplate.exportTemplate(dmpId);
      case "HORIZON_EUROPE" -> exportHorizonEuropeTemplate.exportTemplate(dmpId);
      default -> exportScienceEuropeTemplate.exportTemplate(dmpId, template.id);
    };
  }
}
