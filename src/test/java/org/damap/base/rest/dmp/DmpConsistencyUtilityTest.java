package org.damap.base.rest.dmp;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.TestProfiles;
import org.damap.base.enums.*;
import org.damap.base.rest.dmp.domain.*;
import org.damap.base.rest.dmp.service.DmpConsistencyUtility;
import org.junit.jupiter.api.Test;

@QuarkusTest
@JBossLog
@TestProfile(TestProfiles.DefaultProfile.class)
class DmpConsistencyUtilityTest {

  @Test
  void testSpecifiedDataConsistency() {
    DmpDO dmpDO = getDmp(EDataKind.SPECIFY);
    DmpConsistencyUtility.enforceDmpConsistency(dmpDO);

    assertEquals(EDataKind.SPECIFY, dmpDO.getDataKind());
    assertEquals(EDataKind.SPECIFY, dmpDO.getReusedDataKind());
    assertNull(dmpDO.getNoDataExplanation());

    assertNotNull(dmpDO.getMetadata());
    assertNotNull(dmpDO.getDataGeneration());
    assertNotNull(dmpDO.getDocumentation());
    assertNotNull(dmpDO.getStructure());
    assertFalse(dmpDO.getDataQuality().isEmpty());
    assertNotNull(dmpDO.getOtherDataQuality());
    assertNotNull(dmpDO.getTargetAudience());
    assertNotNull(dmpDO.getTools());
    assertNotNull(dmpDO.getRestrictedDataAccess());

    assertFalse(dmpDO.getDatasets().isEmpty());
    assertFalse(dmpDO.getRepositories().isEmpty());
    assertFalse(dmpDO.getStorage().isEmpty());
    assertFalse(dmpDO.getExternalStorage().isEmpty());

    assertNotNull(dmpDO.getExternalStorageInfo());
    assertNotNull(dmpDO.getRestrictedAccessInfo());
    assertNotNull(dmpDO.getClosedAccessInfo());

    assertTrue(dmpDO.getCostsExist());
    assertTrue(dmpDO.getCostsExistCris());
    assertFalse(dmpDO.getCosts().isEmpty());
  }

  @Test
  void testSpecifiedConditionalDataConsistency() {
    DmpDO dmpDO = getDmp(EDataKind.SPECIFY);
    DmpConsistencyUtility.enforceDmpConsistency(dmpDO);

    assertTrue(dmpDO.getPersonalData());
    assertFalse(dmpDO.getPersonalDataCompliance().isEmpty());
    assertNull(dmpDO.getOtherPersonalDataCompliance());

    assertTrue(dmpDO.getSensitiveData());
    assertFalse(dmpDO.getSensitiveDataSecurity().isEmpty());
    assertNotNull(dmpDO.getOtherDataSecurityMeasures());
    assertNotNull(dmpDO.getSensitiveDataAccess());

    assertTrue(dmpDO.getLegalRestrictions());
    assertFalse(dmpDO.getLegalRestrictionsDocuments().isEmpty());
    assertNotNull(dmpDO.getOtherLegalRestrictionsDocument());
    assertNotNull(dmpDO.getLegalRestrictionsComment());
    assertNotNull(dmpDO.getDataRightsAndAccessControl());

    assertTrue(dmpDO.getHumanParticipants());

    assertTrue(dmpDO.getEthicalIssuesExist());

    assertTrue(dmpDO.getCommitteeReviewed());
  }

  @Test
  void testPartialSpecifiedDataConsistency() {
    DmpDO dmpDO = getDmp(EDataKind.SPECIFY);
    dmpDO.setReusedDataKind(EDataKind.UNKNOWN);
    DmpConsistencyUtility.enforceDmpConsistency(dmpDO);

    assertEquals(EDataKind.SPECIFY, dmpDO.getDataKind());
    assertEquals(EDataKind.UNKNOWN, dmpDO.getReusedDataKind());
    assertNull(dmpDO.getNoDataExplanation());
    assertEquals(1, dmpDO.getDatasets().size());
    DatasetDO newDatasetDO = dmpDO.getDatasets().get(0);
    assertEquals(EDataSource.NEW, newDatasetDO.getSource());

    assertNotNull(dmpDO.getMetadata());
    assertNotNull(dmpDO.getDataGeneration());
    assertNotNull(dmpDO.getDocumentation());
    assertNotNull(dmpDO.getStructure());
    assertFalse(dmpDO.getDataQuality().isEmpty());
    assertNotNull(dmpDO.getOtherDataQuality());
    assertNotNull(dmpDO.getTargetAudience());
    assertNotNull(dmpDO.getTools());
    assertNull(dmpDO.getRestrictedDataAccess());

    assertEquals(
        List.of(newDatasetDO.getReferenceHash()), dmpDO.getRepositories().get(0).getDatasets());
    assertEquals(List.of(newDatasetDO.getReferenceHash()), dmpDO.getStorage().get(0).getDatasets());
    assertEquals(
        List.of(newDatasetDO.getReferenceHash()), dmpDO.getExternalStorage().get(0).getDatasets());

    assertNotNull(dmpDO.getExternalStorageInfo());
    assertNull(dmpDO.getRestrictedAccessInfo());
    assertNotNull(dmpDO.getClosedAccessInfo());
  }

  @Test
  void testSpecifiedDataSetConsistency() {
    DmpDO dmpDO = getDmp(EDataKind.SPECIFY);
    dmpDO.setSensitiveData(false);
    dmpDO.setPersonalData(false);
    dmpDO.setLegalRestrictions(false);

    DmpConsistencyUtility.enforceDmpConsistency(dmpDO);

    DatasetDO newDatasetDO = dmpDO.getDatasets().get(0);
    assertEquals(EDataSource.NEW, newDatasetDO.getSource());
    assertFalse(newDatasetDO.getSensitiveData());
    assertFalse(newDatasetDO.getPersonalData());
    assertFalse(newDatasetDO.getLegalRestrictions());
    assertEquals(EDataAccessType.CLOSED, newDatasetDO.getDataAccess());
    assertTrue(newDatasetDO.getDelete());
    assertNotNull(newDatasetDO.getDeletionPerson());
    assertNotNull(newDatasetDO.getDateOfDeletion());
    assertNotNull(newDatasetDO.getReasonForDeletion());

    DatasetDO reusedDatasetDO = dmpDO.getDatasets().get(1);
    assertEquals(EDataSource.REUSED, reusedDatasetDO.getSource());
    assertFalse(reusedDatasetDO.getSensitiveData());
    assertFalse(reusedDatasetDO.getPersonalData());
    assertFalse(reusedDatasetDO.getLegalRestrictions());
    assertFalse(reusedDatasetDO.getDelete());
    assertEquals(EDataAccessType.RESTRICTED, reusedDatasetDO.getDataAccess());
    assertNull(reusedDatasetDO.getDeletionPerson());
    assertNull(reusedDatasetDO.getDateOfDeletion());
    assertNull(reusedDatasetDO.getReasonForDeletion());
  }

  @Test
  void testNoDataConsistency() {
    DmpDO dmpDO = getDmp(EDataKind.NONE);
    DmpConsistencyUtility.enforceDmpConsistency(dmpDO);

    assertEquals(EDataKind.NONE, dmpDO.getDataKind());
    assertEquals(EDataKind.NONE, dmpDO.getReusedDataKind());
    assertNotNull(dmpDO.getNoDataExplanation());

    assertNull(dmpDO.getMetadata());
    assertNull(dmpDO.getDataGeneration());
    assertNull(dmpDO.getDocumentation());
    assertNull(dmpDO.getStructure());
    assertTrue(dmpDO.getDataQuality().isEmpty());
    assertNull(dmpDO.getOtherDataQuality());
    assertNull(dmpDO.getTargetAudience());
    assertNull(dmpDO.getTools());
    assertNull(dmpDO.getRestrictedDataAccess());

    assertTrue(dmpDO.getDatasets().isEmpty());
    assertTrue(dmpDO.getRepositories().isEmpty());
    assertTrue(dmpDO.getStorage().isEmpty());
    assertTrue(dmpDO.getExternalStorage().isEmpty());

    assertNull(dmpDO.getExternalStorageInfo());
    assertNull(dmpDO.getRestrictedAccessInfo());
    assertNull(dmpDO.getClosedAccessInfo());

    assertFalse(dmpDO.getCostsExist());
    assertTrue(dmpDO.getCostsExistCris());
    assertTrue(dmpDO.getCosts().isEmpty());
  }

  @Test
  void testNoConditionalData() {
    DmpDO dmpDO = getDmp(EDataKind.NONE);
    DmpConsistencyUtility.enforceDmpConsistency(dmpDO);

    assertTrue(dmpDO.getPersonalData());
    assertFalse(dmpDO.getPersonalDataCris());
    assertTrue(dmpDO.getPersonalDataCompliance().isEmpty());
    assertNull(dmpDO.getOtherPersonalDataCompliance());

    assertTrue(dmpDO.getSensitiveData());
    assertNull(dmpDO.getSensitiveDataCris());
    assertTrue(dmpDO.getSensitiveDataSecurity().isEmpty());
    assertNull(dmpDO.getOtherDataSecurityMeasures());
    assertNull(dmpDO.getSensitiveDataAccess());

    assertTrue(dmpDO.getLegalRestrictions());
    assertTrue(dmpDO.getLegalRestrictionsCris());
    assertTrue(dmpDO.getLegalRestrictionsDocuments().isEmpty());
    assertNull(dmpDO.getOtherLegalRestrictionsDocument());
    assertNull(dmpDO.getLegalRestrictionsComment());
    assertNull(dmpDO.getDataRightsAndAccessControl());

    assertTrue(dmpDO.getHumanParticipants());
    assertNull(dmpDO.getHumanParticipantsCris());

    assertTrue(dmpDO.getEthicalIssuesExist());
    assertFalse(dmpDO.getEthicalIssuesExistCris());

    assertTrue(dmpDO.getCommitteeReviewed());
    assertTrue(dmpDO.getCommitteeReviewedCris());
  }

  @Test
  void testUnknownDataConsistency() {
    DmpDO dmpDO = getDmp(EDataKind.UNKNOWN);
    DmpConsistencyUtility.enforceDmpConsistency(dmpDO);

    assertEquals(EDataKind.UNKNOWN, dmpDO.getDataKind());
    assertEquals(EDataKind.UNKNOWN, dmpDO.getReusedDataKind());
    assertNull(dmpDO.getNoDataExplanation());
  }

  private DmpDO getDmp(EDataKind kind) {
    DmpDO dmpDO = this.getBaseDmpDO();
    dmpDO.setDataKind(kind);
    dmpDO.setReusedDataKind(kind);
    return dmpDO;
  }

  private DmpDO getBaseDmpDO() {
    DmpDO dmpDO = new DmpDO();
    dmpDO.setNoDataExplanation("No data");
    // Datasets
    List<DatasetDO> datasets = List.of(this.getNewDataset(), this.getReusedDataset());
    dmpDO.setDatasets(datasets);
    // Documentation & Quality
    dmpDO.setMetadata("Metadata");
    dmpDO.setDataGeneration("Data generation");
    dmpDO.setStructure("Structure");
    dmpDO.setDocumentation("Documentation");
    dmpDO.setDataQuality(List.of(EDataQualityType.OTHERS));
    dmpDO.setOtherDataQuality("Other data quality");
    dmpDO.setTargetAudience("Target audience");
    dmpDO.setTools("Tools");
    // Legal issues
    dmpDO.setSensitiveData(true);
    dmpDO.setSensitiveDataCris(null);
    dmpDO.setSensitiveDataSecurity(List.of(ESecurityMeasure.OTHER));
    dmpDO.setOtherDataSecurityMeasures("Other security measures");
    dmpDO.setSensitiveDataAccess("This is how you get access");
    dmpDO.setPersonalData(true);
    dmpDO.setPersonalDataCris(false);
    dmpDO.setPersonalDataCompliance(List.of(EComplianceType.ANONYMISATION));
    dmpDO.setOtherPersonalDataCompliance("Other measures taken");
    dmpDO.setLegalRestrictions(true);
    dmpDO.setLegalRestrictionsCris(true);
    dmpDO.setLegalRestrictionsDocuments(List.of(EAgreement.OTHER));
    dmpDO.setOtherLegalRestrictionsDocument("Other document");
    dmpDO.setLegalRestrictionsComment("Comment");
    dmpDO.setDataRightsAndAccessControl("Researchers have rights and control access to data");
    dmpDO.setHumanParticipants(true);
    dmpDO.setHumanParticipantsCris(null);
    dmpDO.setEthicalIssuesExist(true);
    dmpDO.setEthicalIssuesExistCris(false);
    dmpDO.setCommitteeReviewed(true);
    dmpDO.setCommitteeReviewedCris(true);
    // Hosts
    dmpDO.setRepositories(getHostList(new RepositoryDO(), datasets));
    dmpDO.setStorage(getHostList(new StorageDO(), datasets));
    dmpDO.setExternalStorage(getHostList(new ExternalStorageDO(), datasets));
    dmpDO.setExternalStorageInfo("External storage info");

    dmpDO.setRestrictedDataAccess("Restricted access");
    dmpDO.setRestrictedAccessInfo("Restricted access info");
    dmpDO.setClosedAccessInfo("Closed access info");

    dmpDO.setCostsExist(true);
    dmpDO.setCostsExistCris(true);
    dmpDO.setCosts(List.of(new CostDO()));

    return dmpDO;
  }

  private DatasetDO getNewDataset() {
    DatasetDO datasetDO = new DatasetDO();
    datasetDO.setSource(EDataSource.NEW);
    datasetDO.setPersonalData(true);
    datasetDO.setSensitiveData(true);
    datasetDO.setLegalRestrictions(true);
    datasetDO.setReferenceHash("new_dataset_hash");
    datasetDO.setDataAccess(EDataAccessType.CLOSED);
    datasetDO.setDelete(true);
    datasetDO.setDateOfDeletion(new Date());
    datasetDO.setDeletionPerson(new ContributorDO());
    datasetDO.setReasonForDeletion("Reason for deletion");
    return datasetDO;
  }

  private DatasetDO getReusedDataset() {
    DatasetDO datasetDO = new DatasetDO();
    datasetDO.setSource(EDataSource.REUSED);
    datasetDO.setPersonalData(false);
    datasetDO.setSensitiveData(false);
    datasetDO.setLegalRestrictions(false);
    datasetDO.setReferenceHash("reused_dataset_hash");
    datasetDO.setDataAccess(EDataAccessType.RESTRICTED);
    datasetDO.setDelete(false);
    datasetDO.setDateOfDeletion(new Date());
    datasetDO.setDeletionPerson(new ContributorDO());
    datasetDO.setReasonForDeletion("Reason for deletion");
    return datasetDO;
  }

  private <R extends HostDO> List<R> getHostList(R host, List<DatasetDO> datasets) {
    host.setDatasets(getReferenceHashes(datasets));
    return List.of(host);
  }

  private List<String> getReferenceHashes(List<DatasetDO> datasets) {
    return datasets.stream().map(DatasetDO::getReferenceHash).collect(Collectors.toList());
  }
}
