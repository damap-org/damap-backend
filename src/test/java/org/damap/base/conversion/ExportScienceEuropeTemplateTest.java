package org.damap.base.conversion;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.damap.base.TestProfiles;
import org.damap.base.TestSetup;
import org.damap.base.integration.mock.MockProjectServiceImpl;
import org.damap.base.rest.dmp.domain.DmpDO;
import org.damap.base.rest.dmp.domain.ProjectDO;
import org.damap.base.rest.dmp.service.DmpService;
import org.damap.base.util.TestDOFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
@TestProfile(TestProfiles.DefaultProfile.class)
class ExportScienceEuropeTemplateTest extends TestSetup {

  @Inject ExportScienceEuropeTemplate exportScienceEuropeTemplate;

  @Inject TestDOFactory testDOFactory;

  @InjectMock MockProjectServiceImpl mockProjectService;

  @Inject DmpService dmpService;

  String projectId = "-1";

  /** setup. */
  @BeforeEach
  @Override
  public void setup() {
    super.setup();
    Mockito.when(mockProjectService.read(projectId)).thenReturn(testDOFactory.getTestProjectDO());
  }

  @Test
  @TestSecurity(authorizationEnabled = false)
  void testFWFTemplateDmp() {
    final DmpDO dmpDO = testDOFactory.getOrCreateTestDmpDO();

    XWPFDocument document = null;
    try {
      document = exportScienceEuropeTemplate.exportTemplate(dmpDO.getId());
    } catch (Exception e) {
      e.printStackTrace();
    }
    Assertions.assertNotNull(document);
  }

  @Test
  @TestSecurity(authorizationEnabled = false)
  void testEmptyFWFTemplateDmp() {
    final DmpDO emptyDmpDO = testDOFactory.getOrCreateTestDmpDOEmpty();

    // testing the export document return not a null document
    XWPFDocument document = null;
    try {
      document = exportScienceEuropeTemplate.exportTemplate(emptyDmpDO.getId());
    } catch (Exception e) {
      e.printStackTrace();
    }
    Assertions.assertNotNull(document);
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
