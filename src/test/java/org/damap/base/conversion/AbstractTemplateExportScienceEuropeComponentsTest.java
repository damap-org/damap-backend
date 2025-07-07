package org.damap.base.conversion;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.damap.base.domain.DatasetSizeRange;
import org.damap.base.integration.mock.MockProjectServiceImpl;
import org.damap.base.integration.orcid.ORCIDPersonServiceImpl;
import org.damap.base.rest.dmp.domain.DmpDO;
import org.damap.base.rest.dmp.domain.ProjectDO;
import org.damap.base.util.MockDmpService;
import org.damap.base.util.TestDOFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
@JBossLog
class AbstractTemplateExportScienceEuropeComponentsTest
    extends AbstractTemplateExportScienceEuropeComponents {

  @Inject TestDOFactory testDOFactory;

  @InjectMock MockProjectServiceImpl mockProjectService;

  @InjectMock ORCIDPersonServiceImpl orcidPersonServiceImpl;

  @Inject MockDmpService dmpService;

  @Inject ExportScienceEuropeTemplate exportScienceEuropeTemplate;

  /** setup. */
  @BeforeEach
  public void setup() {
    Mockito.when(mockProjectService.read(anyString())).thenReturn(testDOFactory.getTestProjectDO());
    Mockito.when(orcidPersonServiceImpl.read(any(String.class)))
        .thenReturn(testDOFactory.getTestContributorDO());
  }

  @Test
  void determineDatasetIDsTest() {
    DmpDO dmpDO = testDOFactory.getOrCreateTestDmpDO();
    exportSetup(dmpDO.getId());
    Assertions.assertEquals(datasetTableIDs.size(), datasets.size(), dmpDO.getDatasets().size());
  }

  @Test
  void testAddLabelForDatasetSize() {
    Assertions.assertEquals("500 - 1000 GB", DatasetSizeRange.getLabelForSize(500_000_000_000L));
  }

  @Test
  void givenDmpHasNoDates_whenDmpIsExported_thenShouldNotFail() {
    DmpDO dmpDO = testDOFactory.getOrCreateTestDmpDO();
    ProjectDO project = dmpDO.getProject();
    project.setStart(null);
    project.setEnd(null);
    dmpDO.setProject(project);
    dmpDO = dmpService.update(dmpDO);

    XWPFDocument document = null;
    try {
      document = exportScienceEuropeTemplate.exportTemplate(dmpDO.getId());
    } catch (Exception e) {
      e.printStackTrace();
    }
    Assertions.assertNotNull(document);
  }
}
