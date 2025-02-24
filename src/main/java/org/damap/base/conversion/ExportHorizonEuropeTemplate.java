package org.damap.base.conversion;

import jakarta.enterprise.context.RequestScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.extern.jbosslog.JBossLog;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.damap.base.domain.Dataset;
import org.damap.base.domain.Repository;
import org.damap.base.enums.EContributorRole;

/** ExportHorizonEuropeTemplate class. */
@RequestScoped
@JBossLog
public class ExportHorizonEuropeTemplate extends AbstractTemplateExportScienceEuropeComponents {
  /**
   * exportTemplate.
   *
   * @param dmpId a long
   * @return a {@link org.apache.poi.xwpf.usermodel.XWPFDocument} object
   */
  public XWPFDocument exportTemplate(long dmpId) {
    log.info("Exporting Horzion Europe document for DMP with ID: " + dmpId);
    // load project
    exportSetup(dmpId);
    // load template and properties
    String startChar = "[";
    String endChar = "]";
    prop = templateFileBrokerService.getHorizonEuropeTemplateResource();
    XWPFDocument document = null;
    try {
      document =
          loadTemplate(templateFileBrokerService.loadHorizonEuropeTemplate(), startChar, endChar);
    } catch (Exception e) {
      log.error("Template file not found!");
      return null;
    }
    xwpfParagraphs = document.getParagraphs();
    xwpfTables = document.getTables();

    // First step of the export: create a mapping of variables and its desired
    // replacement values
    // load replacements values for science europe sections
    loadHorizonEuropeContent();

    // Second step of the export: variables replacement with a mapping reference
    // that has been defined
    log.debug("Export steps: Replace in paragraph");
    replaceInParagraphs(xwpfParagraphs, replacements);

    // Third step of the export: dynamic table in all sections will be added from
    // row number two until the end of data list.
    // TO DO: combine the function with the first row generation to avoid double
    // code of similar modification.
    log.debug("Export steps: Replace in table");
    tableContent(document, xwpfTables);

    // Fourth step of the export: modify the content of the document's footer
    log.debug("Export steps: Replace in footer");
    replaceTextInFooter(document, footerMap);

    return document;
  }

  /** loadHorizonEuropeContent. */
  public void loadHorizonEuropeContent() {
    super.loadScienceEuropeContent();
    workPackageLeadersInformation();
  }

  /** {@inheritDoc} */
  @Override
  public void datasetsInformation() {
    super.datasetsInformation();

    var dataManagers =
        getContributorsByRole(dmp.getContributorList(), Set.of(EContributorRole.DATA_MANAGER));

    if (dataManagers.isEmpty()) {
      addReplacement(
          replacements,
          "[datamanager]",
          loadResourceService.loadVariableFromResource(prop, "datamanager.none"));
      addReplacement(
          replacements,
          "[datamanagerInfo]",
          loadResourceService.loadVariableFromResource(prop, "datamanagerInfo.singular"));
    } else {
      addReplacement(replacements, "[datamanager]", getContributorsText(dataManagers));
      if (dataManagers.size() > 1) {
        addReplacement(
            replacements,
            "[datamanagerInfo]",
            loadResourceService.loadVariableFromResource(prop, "datamanagerInfo.plural"));
      } else {
        addReplacement(
            replacements,
            "[datamanagerInfo]",
            loadResourceService.loadVariableFromResource(prop, "datamanagerInfo.singular"));
      }
    }
  }

  /** workPackageLeadersInformation. */
  public void workPackageLeadersInformation() {
    var workPackageLeaders =
        getContributorsByRole(
            dmp.getContributorList(), Set.of(EContributorRole.WORK_PACKAGE_LEADER));
    if (workPackageLeaders.isEmpty()) {
      addReplacement(
          replacements,
          "[workPackageLeaders]",
          loadResourceService.loadVariableFromResource(prop, "workPackageManger.none"));
    } else {
      addReplacement(replacements, "[workPackageLeaders]", getContributorsText(workPackageLeaders));
    }
  }

  /** {@inheritDoc} */
  @Override
  public void composeTableDatasetRepository(XWPFTable xwpfTable) {
    log.debug("Export steps: Dataset Repository Table");

    List<Dataset> newDatasets =
        getNewDatasets().stream().filter(dataset -> !dataset.getDelete()).toList();
    if (!newDatasets.isEmpty()) {
      for (int i = 0; i < newDatasets.size(); i++) {

        XWPFTableRow sourceTableRow = xwpfTable.getRow(2);
        XWPFTableRow newRow = new XWPFTableRow(sourceTableRow.getCtRow(), xwpfTable);

        try {
          newRow = insertNewTableRow(sourceTableRow, i + 2);
        } catch (Exception e) {
          // new row could not be inserted. Silently fail for now.
          log.error("Could not insert row into dataset table", e);
          continue;
        }

        ArrayList<String> docVar = new ArrayList<>();
        docVar.add(datasetTableIDs.get(newDatasets.get(i).id));
        List<Repository> repositories = newDatasets.get(i).getRepositories();
        if (!repositories.isEmpty()) {
          List<String> repositoryTitles = repositories.stream().map(Repository::getTitle).toList();
          docVar.add(joinWithComma(repositoryTitles));
        } else {
          docVar.add("");
        }

        if (newDatasets.get(i).getRetentionPeriod() != null)
          docVar.add(newDatasets.get(i).getRetentionPeriod() + " years");
        else docVar.add("");

        insertTableCells(xwpfTable, newRow, docVar);
      }
      xwpfTable.removeRow(xwpfTable.getRows().size() - 1);
    } else {
      // clean row
      ArrayList<String> emptyContent = new ArrayList<>(List.of("", "", ""));
      insertTableCells(
          xwpfTable, xwpfTable.getRows().get(xwpfTable.getRows().size() - 1), emptyContent);
    }
    xwpfTable.removeRow(1);
    commitTableRows(xwpfTable);
  }
}
