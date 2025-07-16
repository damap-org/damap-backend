package org.damap.base.integration.pure;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.io.Resources;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.*;
import lombok.SneakyThrows;
import org.damap.base.enums.EContributorRole;
import org.damap.base.enums.EIdentifierType;
import org.damap.base.integration.ProjectServiceProvider;
import org.damap.base.rest.ProjectService;
import org.damap.base.rest.dmp.domain.ContributorDO;
import org.damap.base.rest.dmp.domain.IdentifierDO;
import org.damap.base.rest.dmp.domain.ProjectDO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(PureTestProfile.class)
public class PureProjectsServiceTest {
  private ProjectService createPersonsService() {
    FileBasedPureAPI api = new FileBasedPureAPI();
    api.personsFile = Resources.getResource("org/damap/base/integration/pure/persons.json");
    api.projectsFile = Resources.getResource("org/damap/base/integration/pure/projects.json");

    PureProjectService projectService = new PureProjectService();
    projectService.contributorRoleMapping = new RoleClassificationMappingConfiguration();
    HashMap<String, EContributorRole> roleMappings = new HashMap<>();
    roleMappings.put("/dk/atira/pure/member", EContributorRole.PROJECT_MEMBER);
    roleMappings.put("/dk/atira/pure/test/projectlead", EContributorRole.PROJECT_LEADER);
    projectService.contributorRoleMapping.configs = roleMappings;
    projectService.projectLeadRoleClassification = "/dk/atira/pure/test/projectlead";
    projectService.descriptionClassification = "/dk/atira/pure/projectdescription";
    projectService.pureAPI = api;

    List<ProjectServiceProvider> services = new ArrayList<>();
    services.add(projectService);
    this.projectService = new ProjectService(services, "elsevier-pure");
    return this.projectService;
  }

  @SneakyThrows
  private ProjectDO getFirstProject() {

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    // Make sure that the timezones are parsed the same way:
    dateFormat.setTimeZone(TimeZone.getTimeZone(JsonFormat.DEFAULT_TIMEZONE));

    ProjectDO expected = new ProjectDO();
    expected.setAcronym("test1");
    expected.setUniversityId("430daa1b-f722-4649-b638-b11e056dbc85");
    expected.setTitle("Test Eins");
    expected.setDescription("Test description");
    expected.setStart(dateFormat.parse("2021-01-01"));
    expected.setEnd(dateFormat.parse("2026-01-01"));
    return expected;
  }

  @SneakyThrows
  private ProjectDO getSecondProject() {

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    dateFormat.setTimeZone(TimeZone.getTimeZone(JsonFormat.DEFAULT_TIMEZONE));

    ProjectDO expected = new ProjectDO();
    expected.setAcronym("test2");
    expected.setUniversityId("4541d528-43ed-4cad-98a4-31c83de7fae8");
    expected.setTitle("Test Zwei");
    expected.setDescription("Test description");
    expected.setStart(dateFormat.parse("2022-01-01"));
    expected.setEnd(dateFormat.parse("2027-01-01"));
    return expected;
  }

  private ContributorDO getFirstContributor() {
    ContributorDO expected = new ContributorDO();
    IdentifierDO id = new IdentifierDO();
    id.setType(EIdentifierType.ORCID);
    id.setIdentifier("0000-0001-2345-6789");
    expected.setPersonId(id);
    expected.setFirstName("Jane");
    expected.setLastName("Doe");
    expected.setMbox("jane.doe@example.com");
    expected.setUniversityId("a8864c16-5264-4d94-955d-8be7e00f26a9");
    HashSet<EContributorRole> roles = new HashSet<>();
    roles.add(EContributorRole.PROJECT_LEADER);
    expected.setRoles(roles);
    return expected;
  }

  private ContributorDO getSecondContributor() {
    ContributorDO expected = new ContributorDO();
    IdentifierDO id = new IdentifierDO();
    // We currently don't support looking up the details of external project
    // members.
    id.setType(EIdentifierType.OTHER);
    id.setIdentifier("05f95b98-9e7e-436e-87e2-f72c703384f0");
    expected.setPersonId(id);
    expected.setFirstName("Bob");
    expected.setLastName("Beispiel");
    expected.setUniversityId("05f95b98-9e7e-436e-87e2-f72c703384f0");
    HashSet<EContributorRole> roles = new HashSet<>();
    roles.add(EContributorRole.PROJECT_MEMBER);
    expected.setRoles(roles);
    return expected;
  }

  @Test
  public void testRead() {
    ProjectService svc = createPersonsService();

    ProjectDO expected1 = getFirstProject();
    Assertions.assertEquals(expected1, svc.read(expected1.getUniversityId()));

    ProjectDO expected2 = getSecondProject();
    Assertions.assertEquals(expected2, svc.read(expected2.getUniversityId()));

    Assertions.assertNull(svc.read("asdf"));
  }

  @Test
  public void testGetProjectStaff() {
    ProjectService svc = createPersonsService();

    ProjectDO project1 = getFirstProject();
    ContributorDO expectedContributor1 = getFirstContributor();
    List<ContributorDO> contributors1 = svc.getProjectStaff(project1.getUniversityId());
    Assertions.assertEquals(1, contributors1.size());
    Assertions.assertEquals(expectedContributor1, contributors1.get(0));

    ProjectDO project2 = getFirstProject();
    ContributorDO expectedContributor2 = getFirstContributor();
    List<ContributorDO> contributors2 = svc.getProjectStaff(project2.getUniversityId());
    Assertions.assertEquals(1, contributors2.size());
    Assertions.assertEquals(expectedContributor1, contributors2.get(0));

    Assertions.assertNull(svc.getProjectStaff("asdf"));
  }

  @Test
  public void testGetProjectLead() {
    ProjectService svc = createPersonsService();

    ProjectDO project1 = getFirstProject();
    ContributorDO expectedContributor1 = getFirstContributor();
    Assertions.assertEquals(expectedContributor1, svc.getProjectLeader(project1.getUniversityId()));

    ProjectDO project2 = getSecondProject();
    Assertions.assertNull(svc.getProjectLeader(project2.getUniversityId()));

    Assertions.assertNull(svc.getProjectLeader("asdf"));
  }

  @Inject PureAPI pureAPI;

  @Inject ProjectService projectService;

  @Test
  public void testWiring() {
    Assertions.assertNotNull(pureAPI);
    Assertions.assertTrue(pureAPI instanceof FileBasedPureAPI);

    Assertions.assertNotNull(projectService);

    ProjectDO expected = getFirstProject();
    Assertions.assertEquals(expected, projectService.read(expected.getUniversityId()));
  }
}
