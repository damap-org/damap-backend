package org.damap.base.conversion;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import java.io.InputStream;
import lombok.extern.jbosslog.JBossLog;
import org.apache.poi.xwpf.usermodel.*;
import org.damap.base.rest.ExportTemplateService;

/** ExportScienceEuropeTemplate class. */
@RequestScoped
@JBossLog
public class ExportScienceEuropeTemplate extends AbstractTemplateExportScienceEuropeComponents {
  @Inject ExportTemplateService exportTemplateService;

  /**
   * exportTemplate.
   *
   * @param dmpId a long
   * @param templateId the unique identifier of the template record in the database. This ID
   *     determines which .docx file is used as the basis:
   *     <ul>
   *       <li>ID 1: Default Science Europe template (falls back to classpath resource).
   *       <li>IDs 2, 3: Default funder templates (FWF, Horizon Europe).
   *       <li>ID &gt; 3: Custom variations of the Science Europe template uploaded by admins.
   *     </ul>
   *
   * @return a {@link org.apache.poi.xwpf.usermodel.XWPFDocument} object
   */
  public XWPFDocument exportTemplate(long dmpId, Long templateId) {
    log.info("Exporting Science Europe document for DMP with ID: " + dmpId);
    // load project
    exportSetup(dmpId);
    // load template and properties
    String startChar = "[";
    String endChar = "]";
    prop = templateFileBrokerService.getScienceEuropeTemplateResource();
    XWPFDocument document = null;
    InputStream templateStream = null;
    try {
      templateStream = exportTemplateService.getTemplateStream(templateId);

      // Fallback to classpath resource if no custom database blob found
      if (templateStream == null) {
        templateStream = templateFileBrokerService.loadScienceEuropeTemplate();
      }
      document = loadTemplate(templateStream, startChar, endChar);
    } catch (Exception e) {
      log.error("Template file not found!");
      log.error(e.getMessage());
      return null;
    }
    xwpfParagraphs = document.getParagraphs();
    xwpfTables = document.getTables();

    // First step of the export: create a mapping of variables and its desired replacement values
    // load replacements values for science europe sections
    loadScienceEuropeContent();

    // Second step of the export: variables replacement with a mapping reference that has been
    // defined
    log.debug("Export steps: Replace in paragraph");
    replaceInParagraphs(xwpfParagraphs, replacements);

    // Third step of the export: dynamic table in all sections will be added from row number two
    // until the end of data list.
    // TO DO: combine the function with the first row generation to avoid double code of similar
    // modification.
    log.debug("Export steps: Replace in table");
    tableContent(document, xwpfTables);

    // Fourth step of the export: modify the content of the document's footer
    log.debug("Export steps: Replace in footer");
    replaceTextInFooter(document, footerMap);

    log.debug("Export steps: Export finished");
    return document;
  }
}
