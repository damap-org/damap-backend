package org.damap.base.conversion;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import org.damap.base.TestProfiles;
import org.damap.base.TestSetup;
import org.damap.base.enums.ETemplateType;
import org.damap.base.rest.dmp.domain.DmpDO;
import org.damap.base.rest.dmp.domain.FundingDO;
import org.damap.base.rest.dmp.domain.ProjectDO;
import org.damap.base.rest.dmp.service.DmpService;
import org.damap.base.util.TestDOFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(TestProfiles.DefaultProfile.class)
class TemplateSelectorServiceImplTest extends TestSetup {

  @Inject TestDOFactory testDOFactory;

  @Inject DmpService dmpService;

  @Inject TemplateSelectorServiceImpl templateSelectorService;

  @Test
  @TestSecurity(authorizationEnabled = false)
  void givenDmpHasUnknownFunding_whenTemplateIsSelected_thenShouldReturnScienceEurope() {
    DmpDO testDMP = testDOFactory.createDmp(this.toString(), true);

    ProjectDO project = testDMP.getProject();
    project.setUniversityId("123456UnknwonFunder");
    project.setDescription("Test unknown funder.");
    FundingDO fundingDO = project.getFunding();
    fundingDO.getFunderId().setIdentifier("501100004955");
    project.setFunding(fundingDO);

    testDMP.setProject(project);
    DmpDO updatedDMP = dmpService.update(testDMP);

    Assertions.assertEquals(
        ETemplateType.SCIENCE_EUROPE, templateSelectorService.selectTemplate(updatedDMP));
  }

  @Test
  @TestSecurity(authorizationEnabled = false)
  void givenDmpHasFWFFunding_whenTemplateIsSelected_thenShouldReturnFWF() {
    DmpDO testDMP = testDOFactory.createDmp(this.toString(), true);

    ProjectDO project = testDMP.getProject();
    project.setUniversityId("123456FWFFunder");
    project.setDescription("Test FWF funder.");
    FundingDO fundingDO = project.getFunding();
    fundingDO.getFunderId().setIdentifier("501100002428");
    project.setFunding(fundingDO);

    testDMP.setProject(project);
    DmpDO updatedDMP = dmpService.update(testDMP);

    Assertions.assertEquals(ETemplateType.FWF, templateSelectorService.selectTemplate(updatedDMP));
  }

  @Test
  @TestSecurity(authorizationEnabled = false)
  void givenDmpHasUnknownFunding_whenTemplateIsSelected_thenShouldReturnHorizonEurope() {
    DmpDO testDMP = testDOFactory.createDmp(this.toString(), true);

    ProjectDO project = testDMP.getProject();
    project.setUniversityId("123456HEFunder");
    project.setDescription("Test HE funder.");
    FundingDO fundingDO = project.getFunding();
    fundingDO.getFunderId().setIdentifier("501100000780");
    project.setFunding(fundingDO);

    testDMP.setProject(project);
    DmpDO updatedDMP = dmpService.update(testDMP);

    Assertions.assertEquals(
        ETemplateType.HORIZON_EUROPE, templateSelectorService.selectTemplate(updatedDMP));
  }
}
