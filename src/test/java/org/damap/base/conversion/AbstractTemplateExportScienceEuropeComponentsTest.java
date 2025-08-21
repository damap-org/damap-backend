package org.damap.base.conversion;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.damap.base.TestProfiles;
import org.damap.base.domain.DatasetSizeRange;
import org.damap.base.integration.mock.MockProjectServiceImpl;
import org.damap.base.integration.mock.MockUniversityPersonServiceImpl;
import org.damap.base.integration.orcid.ORCIDMapper;
import org.damap.base.integration.orcid.ORCIDPersonServiceImpl;
import org.damap.base.rest.dmp.domain.DmpDO;
import org.damap.base.rest.dmp.domain.ProjectDO;
import org.damap.base.rest.dmp.service.DmpService;
import org.damap.base.util.TestDOFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
@JBossLog
@TestProfile(TestProfiles.DefaultProfile.class)
class AbstractTemplateExportScienceEuropeComponentsTest
    extends AbstractTemplateExportScienceEuropeComponents {

  @Inject TestDOFactory testDOFactory;

  @InjectMock MockProjectServiceImpl projectService;

  @InjectMock MockUniversityPersonServiceImpl personService;

  @InjectMock ORCIDPersonServiceImpl orcidPersonServiceImpl;

  /** setup. */
  @BeforeEach
  public void setup() {
    Mockito.when(personService.read(any(String.class)))
            .thenReturn(testDOFactory.getTestContributorDO());
    Mockito.when(orcidPersonServiceImpl.read(any(String.class)))
            .thenReturn(ORCIDMapper.mapRecordEntityToPersonDO(testDOFactory.getORCIDTestRecord()));
    Mockito.when(projectService.getProjectLeader(any()))
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
}
