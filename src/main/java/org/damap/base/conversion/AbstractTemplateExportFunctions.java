package org.damap.base.conversion;

import static org.damap.base.utils.EqualityUtils.nullExclusiveEquals;

import jakarta.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;
import lombok.extern.jbosslog.JBossLog;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlException;
import org.damap.base.repo.DmpRepo;
import org.damap.base.repo.InternalStorageTranslationRepo;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;

/** Abstract AbstractTemplateExportFunctions class. */
@JBossLog
public abstract class AbstractTemplateExportFunctions {

  @Inject DmpRepo dmpRepo;

  @Inject InternalStorageTranslationRepo internalStorageTranslationRepo;

  // Convert the date for readable format for the document
  protected final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

  /**
   * Method to load the export template
   *
   * @param template a {@link java.io.InputStream} object
   * @param startChar a {@link java.lang.String} object
   * @param endChar a {@link java.lang.String} object
   * @throws java.lang.Exception
   * @return a {@link org.apache.poi.xwpf.usermodel.XWPFDocument} object
   */
  public XWPFDocument loadTemplate(InputStream template, String startChar, String endChar)
      throws Exception {
    // Extract document using Apache POI https://poi.apache.org/
    XWPFDocument document = new XWPFDocument(template);

    return templateFormatting(document, startChar, endChar);
  }

  /**
   * Method to replace variable in the document's paragraphs Each XWPFRun will contain part of a
   * text. These are split weirdly (by Word?). Special characters will usually be separated from
   * strings, but might be connected if several words are within that textblock. Also capitalized
   * letters seem to behave differently and are sometimes separated from the characters following
   * them.
   *
   * @param xwpfParagraphs
   * @param replacements
   */
  static void replaceInParagraphs(
      List<XWPFParagraph> xwpfParagraphs, Map<String, String> replacements) {
    for (XWPFParagraph xwpfParagraph : xwpfParagraphs) {
      List<XWPFRun> xwpfRuns = xwpfParagraph.getRuns();
      for (XWPFRun xwpfRun : xwpfRuns) {
        String xwpfRunText = xwpfRun.getText(xwpfRun.getTextPosition());
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
          if (xwpfRunText != null && xwpfRunText.contains(entry.getKey())) {
            // handle new line for contributor list and storage information
            if (entry.getValue().contains(";")) {
              xwpfParagraph.setAlignment(ParagraphAlignment.LEFT);
              String[] value = entry.getValue().split(";");
              addContentAsNewLines(xwpfRun, List.of(value), 2);
              xwpfRunText = "";
            }
            // general case for non contributor list
            else {
              if (nullExclusiveEquals(entry.getKey(), "[projectname]")
                  && entry.getValue().contains("#oversize")) { // resize title to be smaller
                xwpfRun.setFontSize(xwpfRun.getFontSize() - 4);
                xwpfRunText =
                    xwpfRunText.replace(entry.getKey(), entry.getValue().replace("#oversize", ""));
              } else if (entry.getValue().contains("#color_green")) { // set the color to be green
                xwpfRun.setColor("92D050");
                xwpfRunText =
                    xwpfRunText.replace(
                        entry.getKey(), entry.getValue().replace("#color_green", ""));
              } else {
                xwpfRunText = xwpfRunText.replace(entry.getKey(), entry.getValue());
              }
            }
          }
        }
        xwpfRun.setText(xwpfRunText, 0);
      }
    }
  }

  static void addContentAsNewLines(XWPFRun xwpfRun, List<String> content, int numberOfNewLines) {
    // TODO: when xwpfRun.removeBreak is implemented:
    // replace the for loop with an enhanced one
    // remove the extra if inside the loop
    // call removeBreak twice outside the loop
    for (int i = 0; i < content.size(); i++) {
      xwpfRun.setText(content.get(i).trim());
      if (i < content.size() - 1) {
        for (int nl = 0; nl < Math.max(1, numberOfNewLines); nl++) {
          xwpfRun.addBreak();
        }
      }
    }
  }

  /**
   * Method to do a replacement of variable with desired value
   *
   * @param replacements a {@link java.util.Map} object
   * @param variable a {@link java.lang.String} object
   * @param dmpContent a {@link java.lang.Object} object
   */
  public void addReplacement(Map<String, String> replacements, String variable, Object dmpContent) {
    String content = (dmpContent == null) ? "" : String.valueOf(dmpContent);
    if (dmpContent != null
        && (dmpContent.getClass() == java.sql.Timestamp.class
            || dmpContent.getClass() == Date.class)) {
      content = formatter.format(dmpContent);
    }

    replacements.put(variable, content);
  }

  /**
   * Method to add new row in a table
   *
   * @param sourceTableRow a {@link org.apache.poi.xwpf.usermodel.XWPFTableRow} object
   * @param pos a int
   * @throws java.io.IOException
   * @throws org.apache.xmlbeans.XmlException
   * @return a {@link org.apache.poi.xwpf.usermodel.XWPFTableRow} object
   */
  public XWPFTableRow insertNewTableRow(XWPFTableRow sourceTableRow, int pos)
      throws XmlException, IOException {
    XWPFTable table = sourceTableRow.getTable();
    CTRow newCTRrow = CTRow.Factory.parse(sourceTableRow.getCtRow().newInputStream());
    XWPFTableRow tableRow = new XWPFTableRow(newCTRrow, table);
    table.addRow(tableRow, pos);
    return tableRow;
  }

  /**
   * Method that is required to be executed to insert data into a table
   *
   * @param table
   * @param newRow
   * @param cellContent
   */
  static void insertTableCells(
      XWPFTable table, XWPFTableRow newRow, ArrayList<String> cellContent) {
    List<XWPFTableCell> cells = newRow.getTableCells();

    int numCells = cells.size();
    int numContent = cellContent.size();

    // Iterate over min(numCells, numContent) to prevent out of bounds
    for (int i = 0; i < Math.min(numCells, numContent); i++) {
      XWPFTableCell cell = cells.get(i);
      List<String> lines = cellContent.get(i).lines().toList();

      // failsafe since sometimes cells dont have a run inside
      if (cell.getParagraphs().isEmpty()) {
        cell.addParagraph().createRun();
      }

      for (XWPFParagraph paragraph : cell.getParagraphs()) {
        for (XWPFRun run : paragraph.getRuns()) {
          // If we do not set the text explicitly on position 0, it will somehow insert the text
          // from the previous row... No real idea why though. So this is a workaround for now.

          if (lines.size() > 1) {
            run.setText("", 0);
            addContentAsNewLines(run, lines, 1);
          } else {
            run.setText(lines.isEmpty() ? "" : lines.get(0), 0);
          }
        }
      }
    }
    commitTableRows(table);
  }

  /**
   * Method that is required to be executed to replace variables in tables
   *
   * @param table
   * @param replacements
   */
  static void replaceTableVariables(XWPFTable table, Map<String, String> replacements) {
    // this replaces variables in tables (e.g. costcurrency)
    List<XWPFTableRow> tableRows = table.getRows();
    for (XWPFTableRow xwpfTableRow : tableRows) {
      List<XWPFTableCell> tableCells = xwpfTableRow.getTableCells();
      for (XWPFTableCell xwpfTableCell : tableCells) {
        List<XWPFParagraph> xwpfParagraphs = xwpfTableCell.getParagraphs();
        replaceInParagraphs(xwpfParagraphs, replacements);
      }
    }
  }

  /**
   * Method that is required to be executed for table content modification
   *
   * @param table
   */
  static void commitTableRows(XWPFTable table) {
    int rowNr = 0;
    for (XWPFTableRow tableRow : table.getRows()) {
      table.getCTTbl().setTrArray(rowNr++, tableRow.getCtRow());
    }
  }

  /**
   * Method to do a replacement specific in document's footer area
   *
   * @param doc a {@link org.apache.poi.xwpf.usermodel.XWPFDocument} object
   * @param replacements a {@link java.util.Map} object
   */
  public void replaceTextInFooter(XWPFDocument doc, Map<String, String> replacements) {
    for (XWPFFooter footer : doc.getFooterList()) {
      for (XWPFParagraph xwpfParagraph : footer.getParagraphs()) {
        for (XWPFRun xwpfRun : xwpfParagraph.getRuns()) {
          String xwpfRunText = xwpfRun.getText(xwpfRun.getTextPosition());
          for (Map.Entry<String, String> entry : replacements.entrySet()) {
            if (xwpfRunText != null && xwpfRunText.contains(entry.getKey())) {
              xwpfRunText = xwpfRunText.replace(entry.getKey(), entry.getValue());
            }
          }
          xwpfRun.setText(xwpfRunText, 0);
        }
      }
    }
  }

  /**
   * Method to combine multiple values become one string. Can be used for generate values from a
   * list of object or replace one variable with multiple values in one execution.
   *
   * @param variableList a {@link java.util.List} object
   * @return a {@link java.lang.String} object
   */
  public String joinWithComma(List<String> variableList) {
    return String.join(", ", variableList);
  }

  /**
   * joinWithCommaAnd.
   *
   * @param variableList a {@link java.util.List} object
   * @return a {@link java.lang.String} object
   */
  public String joinWithCommaAnd(List<String> variableList) {
    return switch (variableList.size()) {
      case 0 -> "";
      case 1 -> variableList.get(0);
      default ->
          String.join(", ", variableList.subList(0, (variableList.size() - 1)))
              + " and "
              + variableList.get(variableList.size() - 1);
    };
  }

  /**
   * Method to format the template. Parsing all text inside the document and preprocessing the
   * variables (currently, text started with "[" and end with "]") that are seperated to multiple
   * runs.
   *
   * @param document a {@link org.apache.poi.xwpf.usermodel.XWPFDocument} object
   * @param startChar a {@link java.lang.String} object
   * @param endChar a {@link java.lang.String} object
   * @throws java.lang.Exception
   * @return a {@link org.apache.poi.xwpf.usermodel.XWPFDocument} object
   */
  public XWPFDocument templateFormatting(XWPFDocument document, String startChar, String endChar)
      throws Exception {

    log.info("Formatting template document");
    // handling all paragraphs in the document except those inside the tables
    formattingParagraph(document.getParagraphs(), startChar, endChar);

    // tables need to handled in independent loop since the paragraph list not return the paragraph
    // inside the tables
    formattingTable(document.getTables(), startChar, endChar);

    // footer need to handled in independent loop since the paragraph list not return the paragraph
    // inside the footer
    if (document.getFooterList() != null) {
      for (XWPFFooter footer : document.getFooterList()) {
        formattingParagraph(footer.getParagraphs(), startChar, endChar);
      }
    }

    return document;
  }

  /**
   * Loop to format tables within tables in the template.
   *
   * @param xwpfTables a {@link java.util.List} object
   * @param startChar a {@link java.lang.String} object
   * @param endChar a {@link java.lang.String} object
   */
  public void formattingTable(List<XWPFTable> xwpfTables, String startChar, String endChar) {
    if (xwpfTables != null) {
      for (XWPFTable xwpfTable : xwpfTables) {
        for (XWPFTableRow row : xwpfTable.getRows()) {
          for (XWPFTableCell cell : row.getTableCells()) {
            formattingParagraph(cell.getParagraphs(), startChar, endChar);
            formattingTable(cell.getTables(), startChar, endChar);
          }
        }
      }
    }
  }

  /**
   * Method to preprocessing format of the paragraph list
   *
   * @param xwpfParagraphs a {@link java.util.List} object
   * @param startChar a {@link java.lang.String} object
   * @param endChar a {@link java.lang.String} object
   */
  public void formattingParagraph(
      List<XWPFParagraph> xwpfParagraphs, String startChar, String endChar) {
    StringBuilder sb = new StringBuilder("");
    boolean mergeRun = false;
    List<Integer> removeRunIndex = new ArrayList<>();

    for (XWPFParagraph xwpfParagraph : xwpfParagraphs) {

      List<XWPFRun> xwpfRuns = xwpfParagraph.getRuns();

      for (XWPFRun xwpfRun : xwpfRuns) {
        String xwpfRunText = xwpfRun.getText(xwpfRun.getTextPosition());

        if (xwpfRunText != null) {
          if (xwpfRunText.contains(startChar)) {
            if (!xwpfRunText.contains(endChar)) {
              removeRunIndex.add(xwpfRuns.indexOf(xwpfRun));
              mergeRun = true;
              if (sb.length() > 0) {
                sb.delete(0, sb.length());
              }
              sb.append(xwpfRunText);
            }
          } else {
            if (mergeRun) {
              sb.append(xwpfRunText);
              if (xwpfRunText.contains(endChar)) {
                mergeRun = false;
                xwpfRun.setText(sb.toString(), 0);
              } else {
                removeRunIndex.add(xwpfRuns.indexOf(xwpfRun));
              }
            }
          }
        }
      }
      if (!removeRunIndex.isEmpty()) {
        Collections.sort(
            removeRunIndex,
            Collections
                .reverseOrder()); // reverse sort index to avoid missing index while deleting the
        // run
        for (Integer runIndex : removeRunIndex) {
          try {
            xwpfParagraphs.get(xwpfParagraphs.indexOf(xwpfParagraph)).removeRun(runIndex);
          } catch (Exception e) {
            log.error("Error while removing run: " + e.getMessage());
          }
        }
        removeRunIndex.clear();
      }
    }
  }

  /**
   * Removes a table inside a document or nested inside another table. For nested tables, this works
   * only for depth 1. If the table is not to be found inside the doc or at depth 1, nothing
   * happens.
   *
   * @param doc a {@link org.apache.poi.xwpf.usermodel.XWPFDocument} object
   * @param table a {@link org.apache.poi.xwpf.usermodel.XWPFTable} object
   */
  public void removeTable(XWPFDocument doc, XWPFTable table) {
    int pos = doc.getPosOfTable(table);
    if (pos != -1) {
      doc.removeBodyElement(pos);
    } else { // table not found in document -> nestedTable
      removeNestedTable(doc, table);
    }
  }

  /**
   * Removes a table which is nested inside another table. Max nested depth is 1.
   *
   * @param doc
   * @param table
   */
  private void removeNestedTable(XWPFDocument doc, XWPFTable table) {
    for (XWPFTableCell cell : getAllOuterTableCells(doc)) {
      for (XWPFTable nestedTable : cell.getTables()) {
        if (nullExclusiveEquals(nestedTable, table)) {
          int pos = cell.getTables().indexOf(nestedTable);
          // dirty hack since POI XWPF does not offer functionality for removing nested tables
          try {
            Field beField = cell.getClass().getDeclaredField("tables");
            beField.setAccessible(true);
            ((List<XWPFTable>) beField.get(cell)).remove(pos); // higher level representation
          } catch (Exception ignored) {
          }
          cell.getCTTc().removeTbl(pos); // low level cell representation

          return;
        }
      }
    }
  }

  /**
   * Removes a table inside a document or nested inside another table. Also removes the paragraph
   * above it. For nested tables, this works only for depth 1. If the table is not to be found
   * inside the doc or at depth 1, nothing happens. If the there is no paragraph above the table,
   * only the table will be removed.
   *
   * @param doc a {@link org.apache.poi.xwpf.usermodel.XWPFDocument} object
   * @param table a {@link org.apache.poi.xwpf.usermodel.XWPFTable} object
   */
  public void removeTableAndParagraphAbove(XWPFDocument doc, XWPFTable table) {
    // paragraph above the table
    if (doc.getPosOfTable(table) != -1) {
      int paragraphPos = doc.getPosOfTable(table) - 1;
      if (nullExclusiveEquals(
          doc.getBodyElements().get(paragraphPos).getElementType(), (BodyElementType.PARAGRAPH))) {
        doc.removeBodyElement(paragraphPos);
      }
    } else {
      for (XWPFTableCell cell : getAllOuterTableCells(doc)) {
        for (XWPFTable nestedTable : cell.getTables()) {
          if (nullExclusiveEquals(nestedTable, table)) {
            int paragraphPos = cell.getBodyElements().indexOf(table) - 1;
            if (nullExclusiveEquals(
                cell.getBodyElements().get(paragraphPos).getElementType(),
                BodyElementType.PARAGRAPH)) {
              XWPFParagraph paragraphToRemove =
                  (XWPFParagraph) cell.getBodyElements().get(paragraphPos);
              cell.removeParagraph(cell.getParagraphs().indexOf(paragraphToRemove));
            }
          }
        }
      }
    }
    // delete unnecessary table
    removeTable(doc, table);
  }

  /**
   * Returns a list of all table cells of non nested tables in the document.
   *
   * @param doc a {@link org.apache.poi.xwpf.usermodel.XWPFDocument} object
   * @return a {@link java.util.List} object
   */
  public List<XWPFTableCell> getAllOuterTableCells(XWPFDocument doc) {
    List<XWPFTableCell> tableCells = new ArrayList<>();
    for (XWPFTable outerTable : doc.getTables()) {
      for (XWPFTableRow row : outerTable.getRows()) {
        tableCells.addAll(row.getTableCells());
      }
    }
    return tableCells;
  }

  /**
   * Returns a list of all tables and nested tables with depth 1.
   *
   * @param doc a {@link org.apache.poi.xwpf.usermodel.XWPFDocument} object
   * @return a {@link java.util.List} object
   */
  public List<XWPFTable> getAllTables(XWPFDocument doc) {
    ArrayList<XWPFTable> tables = new ArrayList<>(doc.getTables());
    for (XWPFTableCell cell : getAllOuterTableCells(doc)) {
      tables.addAll(cell.getTables());
    }
    return tables;
  }

  /**
   * Replaces a run with a hyperlink version of itself. Replaces styling, but keeps fontsize and
   * font family. As of apache 4.1.2 there is no way to insert a hyperlink run anywhere but the end.
   * Therefore, the whole paragraph has to be deleted and rebuilt after the run in question.
   *
   * @param run
   * @param URI
   */
  void turnRunIntoHyperlinkRun(XWPFRun run, String URI) {
    XWPFParagraph paragraph = (XWPFParagraph) run.getParent();
    int runPos = paragraph.getRuns().indexOf(run);
    int runsAfterHyperlinkRun = paragraph.getRuns().size() - runPos - 1;

    XWPFHyperlinkRun hyperlink = paragraph.createHyperlinkRun(URI);
    hyperlink.setStyle("Hyperlink");
    hyperlink.setFontSize(run.getFontSize());
    hyperlink.setFontFamily(run.getFontFamily());
    // For multiple reused datasets, the run at index runPos is already an XWPFHyperlink run
    // (bug, should be fixed in the future) causing removeRun from the XPWFParagraph class
    // to throw an exception. For this reason, we don't need to set the text nor delete the run if
    // we are in this situation.
    if (!(paragraph.getRuns().get(runPos) instanceof XWPFHyperlinkRun)) {
      hyperlink.setText(run.getText(0));
    }
    if (!(paragraph.getRuns().get(runPos) instanceof XWPFHyperlinkRun)) {
      paragraph.removeRun(runPos);
    }
    for (int i = 0; i < runsAfterHyperlinkRun; i++) {
      XWPFRun runToRemove = paragraph.getRuns().get(runPos);

      XWPFRun createdRun = paragraph.createRun();
      createdRun.getCTR().setRPr(runToRemove.getCTR().getRPr());
      createdRun.setText(runToRemove.getText(0));

      paragraph.removeRun(runPos);
    }
  }
}
