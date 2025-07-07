package org.damap.base.conversion;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.damap.base.TestSetup;
import org.damap.base.integration.mock.MockProjectServiceImpl;
import org.damap.base.rest.dmp.domain.DmpDO;
import org.damap.base.util.TestDOFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ExportHorizonEuropeTemplateTest extends TestSetup {

  @Inject ExportHorizonEuropeTemplate exportHorizonEuropeTemplate;

  @Inject TestDOFactory testDOFactory;

  @InjectMock MockProjectServiceImpl mockProjectService;

  @Test
  @TestSecurity(authorizationEnabled = false)
  void testTemplateDmp() {
    final DmpDO dmpDO = testDOFactory.getOrCreateTestDmpDO();

    XWPFDocument document = null;
    try {
      document = exportHorizonEuropeTemplate.exportTemplate(dmpDO.getId());
    } catch (Exception e) {
      e.printStackTrace();
    }
    Assertions.assertNotNull(document);
  }

  @Test
  @TestSecurity(authorizationEnabled = false)
  void testEmptyTemplateDmp() {
    final DmpDO emptyDmpDO = testDOFactory.getOrCreateTestDmpDOEmpty();

    // testing the export document return not a null document
    XWPFDocument document = null;
    try {
      document = exportHorizonEuropeTemplate.exportTemplate(emptyDmpDO.getId());
    } catch (Exception e) {
      e.printStackTrace();
    }
    Assertions.assertNotNull(document);
  }
}
