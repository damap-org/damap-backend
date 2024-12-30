package org.damap.base.conversion;

import jakarta.enterprise.context.RequestScoped;
import java.util.*;
import lombok.extern.jbosslog.JBossLog;
import org.apache.poi.xwpf.usermodel.*;

/** ExportFWFTemplate class. */
@RequestScoped
@JBossLog
public class ExportFWFTemplate extends AbstractTemplateExportScienceEuropeComponents {

  /**
   * exportTemplate.
   *
   * @param dmpId a long
   * @return a {@link org.apache.poi.xwpf.usermodel.XWPFDocument} object
   */
  public XWPFDocument exportTemplate(long dmpId) {
    log.info("Exporting FWF document for DMP with ID: " + dmpId);
    // load project
    exportSetup(dmpId);
    // load template and properties
    String startChar = "[";
    String endChar = "]";
    prop = templateFileBrokerService.getFWFTemplateResource();
    XWPFDocument document = null;
    try {
      document = loadTemplate(templateFileBrokerService.loadFWFTemplate(), startChar, endChar);
    } catch (Exception e) {
      log.error("Template file not found!");
      return null;
    }
    xwpfParagraphs = document.getParagraphs();
    xwpfTables = document.getTables();
    // get template table containing all information that needs to be replaced.
    XWPFTable templateTable = xwpfTables.get(2);
    // get tables within template table
    List<XWPFTable> templateXwpfTables = parseContentTables(templateTable);

    // load replacements values for science europe sections
    loadScienceEuropeContent();
    replaceInParagraphs(xwpfParagraphs, replacements);
    // in FWF template this replaces paragraphs within the template table
    replaceTableVariables(templateTable, replacements);
    // this replaces the tables with the main template table
    tableContent(document, templateXwpfTables);

    return document;
  }

  private List<XWPFTable> parseContentTables(XWPFTable templateTable) {
    List<XWPFTable> templateXwpfTables = new ArrayList<>();
    for (XWPFTableRow row : templateTable.getRows()) {
      if (row.getTableCells().size() > 1) {
        templateXwpfTables.addAll(row.getCell(1).getTables());
      }
    }
    return templateXwpfTables;
  }
}
