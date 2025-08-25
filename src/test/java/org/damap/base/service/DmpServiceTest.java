package org.damap.base.service;

import static org.junit.jupiter.api.Assertions.assertNull;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.extern.java.Log;
import org.damap.base.TestProfiles;
import org.damap.base.TestSetup;
import org.damap.base.domain.*;
import org.damap.base.enums.EContributorRole;
import org.damap.base.enums.EIdentifierType;
import org.damap.base.integration.orcid.models.ORCIDRecord;
import org.damap.base.rest.dmp.domain.*;
import org.damap.base.rest.dmp.service.DmpService;
import org.damap.base.util.TestDOFactory;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(TestProfiles.DefaultProfile.class)
@Log
class DmpServiceTest extends TestSetup {
  @Inject TestDOFactory testDOFactory;

  @Inject DmpService dmpService;

  @ConfigProperty(name = "damap.projects-service")
  String activeProfile;

  @Test
  void updateProjectLeadTest() {
    System.out.println(activeProfile);
    ProjectDO projectDO = new ProjectDO();
    projectDO.setUniversityId("-1");

    DmpDO dmpDO = new DmpDO();
    dmpDO.setTitle("title");
    dmpDO.setProject(projectDO);

    dmpDO = dmpService.create(dmpDO, "editedBy");

    Assertions.assertFalse(dmpDO.getContributors().isEmpty());
    Optional<ContributorDO> projectLead =
        dmpDO.getContributors().stream()
            .filter(
                c -> c.getRoles() != null && c.getRoles().contains(EContributorRole.PROJECT_LEADER))
            .findFirst();
    Assertions.assertTrue(projectLead.isPresent());
    Assertions.assertTrue(projectLead.get().isContact());

    long projectLeadID = projectLead.get().getId();

    // Remove project from dmp and update. Nothing should happen.
    dmpDO.setProject(null);
    dmpDO = dmpService.update(dmpDO);

    projectLead.get().setRoles(new HashSet<>(Set.of(EContributorRole.PROJECT_MANAGER)));
    projectLead.get().setContact(false);

    ContributorDO otherContributor = new ContributorDO();
    otherContributor.setContact(true);

    dmpDO.setContributors(Arrays.asList(projectLead.get(), otherContributor));

    dmpDO.setProject(projectDO);
    dmpDO = dmpService.update(dmpDO);

    Assertions.assertEquals(2, dmpDO.getContributors().size());

    projectLead =
        dmpDO.getContributors().stream().filter(c -> c.getId().equals(projectLeadID)).findFirst();

    otherContributor =
        dmpDO.getContributors().stream()
            .filter(c -> !c.getId().equals(projectLeadID))
            .findFirst()
            .get();

    Assertions.assertTrue(projectLead.isPresent());
    Assertions.assertFalse(projectLead.get().isContact());
    Assertions.assertTrue(otherContributor.isContact());
    Assertions.assertTrue(projectLead.get().getRoles().contains(EContributorRole.PROJECT_MANAGER));
    Assertions.assertTrue(projectLead.get().getRoles().contains(EContributorRole.PROJECT_LEADER));

    dmpDO.setProject(null);
    dmpDO = dmpService.update(dmpDO);

    // Remove other contributor and set role of project lead to null.
    projectLead.get().setRoles(null);
    dmpDO.setContributors(Arrays.asList(projectLead.get()));

    dmpDO.setProject(projectDO);
    dmpDO = dmpService.update(dmpDO);

    Assertions.assertEquals(1, dmpDO.getContributors().size());
    projectLead =
        dmpDO.getContributors().stream().filter(c -> c.getId().equals(projectLeadID)).findFirst();

    Assertions.assertTrue(projectLead.get().isContact());
    Assertions.assertEquals(Set.of(EContributorRole.PROJECT_LEADER), projectLead.get().getRoles());
  }

  @Test
  void fetchORCIDContributorInfo() {
    ORCIDRecord testRecord = testDOFactory.getORCIDTestRecord();

    DmpDO dmpDO = new DmpDO();
    dmpDO.setTitle("fetchORCIDContributorInfo");
    ContributorDO orcidContributorDO = new ContributorDO();

    IdentifierDO orcidIdentifier = new IdentifierDO();
    orcidIdentifier.setType(EIdentifierType.ORCID);
    orcidIdentifier.setIdentifier("orcid");

    orcidContributorDO.setPersonId(orcidIdentifier);

    dmpDO.setContributors(List.of(orcidContributorDO));

    dmpDO = dmpService.create(dmpDO, "");
    var contributorDOs = dmpDO.getContributors();
    Assertions.assertFalse(contributorDOs.isEmpty());
    Assertions.assertEquals(
        testRecord.getPerson().getName().getGivenNames().getValue(),
        contributorDOs.get(0).getFirstName());

    Assertions.assertEquals(
        testRecord.getPerson().getName().getFamilyName().getValue(),
        contributorDOs.get(0).getLastName());
  }

  @Test
  void givenRepositoryIsRemoved_whenUpdatingDMP_thenRepositoryShouldBeRemoved() {
    DmpDO testDMP = testDOFactory.createDmp(this.toString(), true);
    Assertions.assertEquals(1, testDMP.getRepositories().size());
    testDMP.setRepositories(new ArrayList<>());
    DmpDO updatedDMP = dmpService.update(testDMP);
    Assertions.assertEquals(0, updatedDMP.getRepositories().size());
  }

  @Test
  void givenRepositoryIsAdded_whenUpdatingDMP_thenRepositoryShouldBeAdded() {
    DmpDO testDMP = testDOFactory.createDmp(this.toString(), true);
    Assertions.assertEquals(1, testDMP.getRepositories().size());

    List<RepositoryDO> repoList = testDMP.getRepositories();
    RepositoryDO repositoryToAdd = new RepositoryDO();
    repositoryToAdd.setRepositoryId("r3d100013558");
    repositoryToAdd.setTitle("TU Data 2");
    repositoryToAdd.setDatasets(List.of("referenceHash123456", "referenceHash234567"));
    repoList.add(repositoryToAdd);

    DmpDO updatedDMP = dmpService.update(testDMP);
    Assertions.assertEquals(2, updatedDMP.getRepositories().size());
  }

  @Test
  void givenDmpProjectAcronymIsUpdated_whenUpdatingDMP_thenProjectAcronymShouldBeUpdated() {
    DmpDO testDMP = testDOFactory.createDmp(this.toString(), true);
    Assertions.assertEquals("TEST", testDMP.getProject().getAcronym());
    testDMP.getProject().setAcronym("newAcronym");
    DmpDO updatedDMP = dmpService.update(testDMP);
    Assertions.assertEquals("newAcronym", updatedDMP.getProject().getAcronym());
  }

  @Test
  void givenDmpWithValidTitle_whenGettingDefaultFileName_thenShouldUseTitle() {
    DmpDO dmpWithTitle = new DmpDO();
    dmpWithTitle.setTitle("Test DMP Title");
    dmpWithTitle = dmpService.create(dmpWithTitle, "testUser");

    String fileName = dmpService.getDefaultFileName(dmpWithTitle.getId());

    Assertions.assertEquals("Test_DMP_Title", fileName);
  }

  @Test
  void givenOldDmpWithNullTitleAndNullProjectTitle_whenGettingDefaultFileName_thenShouldUseDmpId() {
    DmpDO oldDmp = new DmpDO();
    oldDmp.setTitle(null);
    ProjectDO project = new ProjectDO();
    project.setTitle(null);
    oldDmp.setProject(project);
    oldDmp = dmpService.create(oldDmp, "testUser");

    String fileName = dmpService.getDefaultFileName(oldDmp.getId());

    Date date = new Date();
    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
    Assertions.assertEquals("DMP_" + oldDmp.getId() + "_" + formatter.format(date), fileName);
  }

  @Test
  void givenValidDmp_whenDeletingDmp_thenShouldAlsoDeleteAllDependantEntities() {
    dmpService.delete(dmpDO.getId());

    // Check if all dependant entities are deleted
    assertNull(Project.findById(dmpDO.getProject().getId()));

    for (ContributorDO contributor : dmpDO.getContributors()) {
      assertNull(Contributor.findById(contributor.getId()));
    }

    for (DatasetDO dataset : dmpDO.getDatasets()) {
      assertNull(Dataset.findById(dataset.getId()));
    }

    for (StorageDO storage : dmpDO.getStorage()) {
      assertNull(Storage.findById(storage.getId()));
    }

    for (RepositoryDO repo : dmpDO.getRepositories()) {
      assertNull(Repository.findById(repo.getId()));
    }

    for (CostDO cost : dmpDO.getCosts()) {
      assertNull(Cost.findById(cost.getId()));
    }
  }
}
