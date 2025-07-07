package org.damap.base.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.domain.Dmp;
import org.damap.base.domain.DmpVersion;
import org.damap.base.domain.InternalStorage;
import org.damap.base.domain.InternalStorageTranslation;
import org.damap.base.enums.EAccessRight;
import org.damap.base.enums.EAgreement;
import org.damap.base.enums.EComplianceType;
import org.damap.base.enums.EContributorRole;
import org.damap.base.enums.ECostType;
import org.damap.base.enums.EDataAccessType;
import org.damap.base.enums.EDataKind;
import org.damap.base.enums.EDataQualityType;
import org.damap.base.enums.EDataSource;
import org.damap.base.enums.EDataType;
import org.damap.base.enums.EFundingState;
import org.damap.base.enums.EIdentifierType;
import org.damap.base.enums.ELicense;
import org.damap.base.enums.ESecurityMeasure;
import org.damap.base.integration.orcid.models.ORCIDRecord;
import org.damap.base.repo.DmpRepo;
import org.damap.base.repo.DmpVersionRepo;
import org.damap.base.repo.InternalStorageRepo;
import org.damap.base.repo.InternalStorageTranslationRepo;
import org.damap.base.rest.dmp.domain.ContributorDO;
import org.damap.base.rest.dmp.domain.CostDO;
import org.damap.base.rest.dmp.domain.DatasetDO;
import org.damap.base.rest.dmp.domain.DmpDO;
import org.damap.base.rest.dmp.domain.ExternalStorageDO;
import org.damap.base.rest.dmp.domain.FundingDO;
import org.damap.base.rest.dmp.domain.IdentifierDO;
import org.damap.base.rest.dmp.domain.ProjectDO;
import org.damap.base.rest.dmp.domain.RepositoryDO;
import org.damap.base.rest.dmp.domain.StorageDO;
import org.damap.base.rest.dmp.mapper.DmpDOMapper;
import org.damap.base.rest.version.VersionDO;
import org.damap.base.rest.version.VersionDOMapper;
import org.damap.base.rest.version.VersionService;

/** TestDOFactory class. */
@JBossLog
@ApplicationScoped
public class TestDOFactory {

  @Inject DmpRepo dmpRepo;

  @Inject InternalStorageTranslationRepo internalStorageTranslationRepo;

  @Inject InternalStorageRepo internalStorageRepo;

  @Inject MockDmpService dmpService;

  @Inject VersionService versionService;

  @Inject DmpVersionRepo dmpVersionRepo;

  @Inject EntityManager entityManager;

  private final String editorId = "012345";

  /**
   * createDmp.
   *
   * @param title a {@link java.lang.String} object
   * @param withDefaultData a boolean
   * @return a {@link org.damap.base.rest.dmp.domain.DmpDO} object
   */
  @Transactional
  public DmpDO createDmp(String title, boolean withDefaultData) {

    if ("TestDmp".equals(title)) {
      // TODO: fix test cases to not depend on the TestDmp. Instead, create a new DMP
      // if required.
      throw new ValidationException(title + "is reserved for the TestDmp");
    }

    DmpDO newTestDmpDO = new DmpDO();
    if (withDefaultData) {
      this.setDataOnDMP(newTestDmpDO);
    }
    newTestDmpDO.setTitle(title);
    return dmpService.create(newTestDmpDO, editorId);
  }

  private void setDataOnDMP(DmpDO dmpDO) {
    dmpDO.setTitle("TestDmp");
    dmpDO.setCreated(new Date());
    dmpDO.setModified(new Date());
    dmpDO.setDescription("This DMP is created for an automated test.");
    dmpDO.setProject(getTestProjectDO());
    dmpDO.setDataKind(EDataKind.NONE);
    dmpDO.setReusedDataKind(EDataKind.UNKNOWN);
    dmpDO.setContributors(getTestContributorList());
    dmpDO.setNoDataExplanation("This is why there are no datasets.");
    dmpDO.setMetadata("String for metadata.");
    dmpDO.setDataGeneration("Text on data generation.");
    dmpDO.setStructure("Structure of the data.");
    dmpDO.setDataQuality(getDataQualityList());
    dmpDO.setOtherDataQuality("Other data quality measures.");
    dmpDO.setTargetAudience("This is the target audience.");
    dmpDO.setTools("Tools used for gathering data.");
    dmpDO.setRestrictedDataAccess("Here is why access to the data is restricted.");
    dmpDO.setPersonalData(true);
    dmpDO.setPersonalDataCompliance(getComplianceTypeList());
    dmpDO.setOtherPersonalDataCompliance("Option for additional data compliance.");
    dmpDO.setSensitiveData(true);
    dmpDO.setSensitiveDataSecurity(getSensitiveDataSecurityList());
    dmpDO.setOtherDataSecurityMeasures("Option for additional security measures.");
    dmpDO.setSensitiveDataAccess("Text for sensitive data access.");
    dmpDO.setLegalRestrictions(true);
    dmpDO.setLegalRestrictionsDocuments(getLegalRestrictionsDocuments());
    dmpDO.setOtherLegalRestrictionsDocument("Option for additional legal restriction documents.");
    dmpDO.setLegalRestrictionsComment("Additional legal restriction comment.");
    dmpDO.setDataRightsAndAccessControl(
        "List of people/institutions having access to the restricted data.");
    dmpDO.setHumanParticipants(true);
    dmpDO.setEthicalIssuesExist(true);
    dmpDO.setCommitteeReviewed(true);
    dmpDO.setDataKind(EDataKind.SPECIFY);
    dmpDO.setReusedDataKind(EDataKind.SPECIFY);
    dmpDO.setDatasets(getTestDatasetList());
    dmpDO.setRepositories(getTestRepositoryList());
    dmpDO.setStorage(getTestStorageList());
    dmpDO.setExternalStorage(getTestExternalStorageList());
    dmpDO.setExternalStorageInfo("Additional Info on the selected external storage.");
    dmpDO.setRestrictedAccessInfo("Additional Info on how restricted access is handled.");
    dmpDO.setClosedAccessInfo("Additional Info on how closed access for the data is handled");
    dmpDO.setCostsExist(true);
    dmpDO.setCosts(getTestCostList());
  }

  /**
   * getOrCreateTestDmpDO.
   *
   * @return a {@link org.damap.base.rest.dmp.domain.DmpDO} object
   */
  @Transactional
  public DmpDO getOrCreateTestDmpDO() {

    prepareInternalStorageOption();

    final Optional<Dmp> testDmp =
        dmpRepo.getAll().stream().filter(a -> "TestDmp".equals(a.getTitle())).findAny();
    if (testDmp.isPresent()) return DmpDOMapper.mapEntityToDO(testDmp.get(), new DmpDO());

    DmpDO newTestDmpDO = new DmpDO();

    this.setDataOnDMP(newTestDmpDO);
    dmpService.create(newTestDmpDO, editorId);
    return getOrCreateTestDmpDO();
  }

  private List<EDataQualityType> getDataQualityList() {
    return Arrays.asList(
        EDataQualityType.REPRESENTATION_WITH_CONTROLLED_VOCABULARIES, EDataQualityType.OTHERS);
  }

  private List<EComplianceType> getComplianceTypeList() {
    return Arrays.asList(EComplianceType.INFORMED_CONSENT, EComplianceType.ANONYMISATION);
  }

  private List<ESecurityMeasure> getSensitiveDataSecurityList() {
    return Arrays.asList(
        ESecurityMeasure.INDIVIDUAL_LOGIN, ESecurityMeasure.AUTOMATIC_LOCKING_OF_CLIENTS);
  }

  private List<EAgreement> getLegalRestrictionsDocuments() {
    return Arrays.asList(EAgreement.CONSORTIUM_AGREEMENT, EAgreement.CONFIDENTIALITY_AGREEMENT);
  }

  /**
   * getTestProjectDO.
   *
   * @return a {@link org.damap.base.rest.dmp.domain.ProjectDO} object
   */
  public ProjectDO getTestProjectDO() {
    ProjectDO project = new ProjectDO();
    project.setAcronym("TEST");
    project.setUniversityId("123456");
    project.setDescription("Test Project description.");
    project.setTitle("Test Project");
    project.setFunding(getTestFundingDO());
    project.setStart(new Date());
    project.setEnd(new Date());
    return project;
  }

  /**
   * getRecommendedTestProjectDO.
   *
   * @return a {@link org.damap.base.rest.dmp.domain.ProjectDO} object
   */
  public ProjectDO getRecommendedTestProjectDO() {
    ProjectDO project = new ProjectDO();
    project.setAcronym("RECOMMENDED");
    project.setUniversityId("123REC");
    project.setDescription("Test Recommended Project description.");
    project.setTitle("Test Recommended Project");
    project.setFunding(getTestFundingDO());
    project.setStart(new Date());
    project.setEnd(new Date());
    return project;
  }

  private FundingDO getTestFundingDO() {
    FundingDO funding = new FundingDO();
    funding.setFundingName("Funder Institutiuon");
    funding.setFundingProgram("Funding Program");
    funding.setFunderId(getTestIdentifierDO(EIdentifierType.FUNDREF));
    funding.setGrantId(getTestIdentifierDO(EIdentifierType.OTHER));
    funding.setFundingStatus(EFundingState.GRANTED);
    return funding;
  }

  private IdentifierDO getTestIdentifierDO(EIdentifierType type) {
    IdentifierDO identifier = new IdentifierDO();
    identifier.setIdentifier("Unique Identifier 123456");
    identifier.setType(type);
    return identifier;
  }

  /**
   * getTestContributorDO.
   *
   * @return a {@link org.damap.base.rest.dmp.domain.ContributorDO} object
   */
  public ContributorDO getTestContributorDO() {
    ContributorDO contributor = new ContributorDO();
    contributor.setUniversityId("Internal Identifier 123456");
    contributor.setPersonId(getTestIdentifierDO(EIdentifierType.ORCID));
    contributor.setFirstName("Jane");
    contributor.setLastName("Doe");
    contributor.setMbox("jane.doe@research.institution.com");
    contributor.setAffiliation("Affiliated Institution");
    contributor.setAffiliationId(getTestIdentifierDO(EIdentifierType.ROR));
    contributor.setContact(true);
    return contributor;
  }

  private List<ContributorDO> getTestContributorList() {
    ContributorDO contributor = getTestContributorDO();
    contributor.setRoles(new HashSet<>(Set.of(EContributorRole.DATA_MANAGER)));

    ContributorDO secondContributor = getTestContributorDO();
    secondContributor.setFirstName("John");
    secondContributor.setLastName("Doe");
    secondContributor.setMbox("john.doe@research.institution.com");
    secondContributor.setRoles(new HashSet<>(Set.of(EContributorRole.DATA_COLLECTOR)));
    secondContributor.setContact(false);

    return Arrays.asList(contributor, secondContributor);
  }

  private List<DatasetDO> getTestDatasetList() {
    DatasetDO dataset = new DatasetDO();
    dataset.setTitle("Dataset Title");
    dataset.setSource(EDataSource.NEW);
    dataset.setType(List.of(EDataType.CONFIGURATION_DATA));
    dataset.setFileFormat(".cfg");
    dataset.setSize(50000L);
    dataset.setDescription("Dataset description.");
    dataset.setRetentionPeriod(10);
    dataset.setPersonalData(true);
    dataset.setSensitiveData(true);
    dataset.setLegalRestrictions(true);
    dataset.setLicense(ELicense.CCBY);
    dataset.setStartDate(new Date());
    dataset.setReferenceHash("referenceHash123456");
    dataset.setDataAccess(EDataAccessType.OPEN);
    dataset.setSelectedProjectMembersAccess(EAccessRight.WRITE);
    dataset.setOtherProjectMembersAccess(EAccessRight.READ);
    dataset.setPublicAccess(EAccessRight.READ);
    dataset.setDelete(true);
    dataset.setDateOfDeletion(new Date());
    dataset.setReasonForDeletion("Explanation on why data is being deleted.");
    dataset.setSource(EDataSource.REUSED);
    IdentifierDO identifierDO = new IdentifierDO();
    identifierDO.setIdentifier("Unique Identifier 123456");
    identifierDO.setType(EIdentifierType.DOI);
    dataset.setDatasetId(identifierDO);
    List<ContributorDO> contributors = getTestContributorList();
    if (!contributors.isEmpty()) dataset.setDeletionPerson(contributors.get(0));

    DatasetDO dataset2 = new DatasetDO();
    dataset2.setTitle("Dataset2 Title");
    dataset2.setSource(EDataSource.NEW);
    dataset2.setReferenceHash("referenceHash234567");

    return List.of(dataset, dataset2);
  }

  private List<RepositoryDO> getTestRepositoryList() {
    RepositoryDO repository = new RepositoryDO();
    repository.setRepositoryId("r3d100010468");
    repository.setTitle("Zenodo");
    repository.setDatasets(List.of("referenceHash123456", "referenceHash234567"));
    return List.of(repository);
  }

  private List<StorageDO> getTestStorageList() {
    StorageDO storage = new StorageDO();
    Optional<InternalStorageTranslation> testInternalStorage =
        internalStorageTranslationRepo.getAllInternalStorageByLanguage("eng").stream()
            .filter(a -> "Test Storage Title".equals(a.getTitle()))
            .findAny();
    testInternalStorage.ifPresent(
        storageTranslation ->
            storage.setInternalStorageId(storageTranslation.getInternalStorageId().id));
    storage.setTitle("Internal Host");
    storage.setDatasets(List.of("referenceHash123456"));

    return List.of(storage);
  }

  private List<ExternalStorageDO> getTestExternalStorageList() {
    ExternalStorageDO storage = new ExternalStorageDO();
    storage.setTitle("External Host");
    storage.setDatasets(List.of("referenceHash123456"));
    storage.setUrl("external.storage.url");
    storage.setBackupFrequency("Frequency of data backups.");
    storage.setStorageLocation("Location of Storages");
    storage.setBackupLocation("Location of Backups");
    storage.setIsManagedInternally(false);
    return List.of(storage);
  }

  private List<CostDO> getTestCostList() {
    CostDO cost = new CostDO();
    cost.setTitle("Cost Item Title");
    cost.setValue(50000f);
    cost.setCurrencyCode("EUR");
    cost.setDescription("Descriptiopn of required expense");
    cost.setType(ECostType.DATABASE);
    cost.setTitle("Custom Typology");
    return List.of(cost);
  }

  /**
   * getOrCreateTestDmpDOEmpty.
   *
   * @return a {@link org.damap.base.rest.dmp.domain.DmpDO} object
   */
  public DmpDO getOrCreateTestDmpDOEmpty() {
    final Optional<Dmp> testDmp =
        dmpRepo.getAll().stream().filter(a -> "EmptyTestDmp".equals(a.getTitle())).findAny();
    if (testDmp.isPresent()) return DmpDOMapper.mapEntityToDO(testDmp.get(), new DmpDO());

    DmpDO newTestDmpDO = new DmpDO();
    newTestDmpDO.setTitle("EmptyTestDmp");

    dmpService.create(newTestDmpDO, editorId);
    return getOrCreateTestDmpDOEmpty();
  }

  /** prepareInternalStorageOption. */
  @Transactional
  public Long prepareInternalStorageOption() {

    InternalStorage internalStorage = new InternalStorage();
    internalStorage.setUrl("test.url.com");
    internalStorage.setStorageLocation("AUT");
    internalStorage.setBackupLocation("AUT");
    internalStorage.setActive(true);
    internalStorage.persist();

    InternalStorageTranslation internalStorageTranslation = new InternalStorageTranslation();
    internalStorageTranslation.setInternalStorageId(internalStorage);
    internalStorageTranslation.setTitle("Test Storage Title");
    internalStorageTranslation.setDescription(
        "Long winded yet brief description of the storage option");
    internalStorageTranslation.setLanguageCode("eng");
    internalStorageTranslation.persistAndFlush();

    InternalStorage internalStorage2 = new InternalStorage();
    internalStorage2.setUrl("test2.url.com");
    internalStorage2.setStorageLocation("AUT");
    internalStorage2.setBackupLocation("GER");
    internalStorage2.setActive(false);
    internalStorage2.persist();

    InternalStorageTranslation internalStorageTranslation2 = new InternalStorageTranslation();
    internalStorageTranslation2.setInternalStorageId(internalStorage2);
    internalStorageTranslation2.setTitle("Test Storage Title");
    internalStorageTranslation2.setDescription(
        "Long winded yet brief description of the storage option");
    internalStorageTranslation2.setLanguageCode("eng");
    internalStorageTranslation2.persistAndFlush();

    return internalStorage.id;
  }

  @Transactional
  public Long prepareSpecialInternalStorageOption(String urlSuffix) {
    InternalStorage internalStorage = new InternalStorage();
    internalStorage.setUrl("special.url.com" + urlSuffix);
    internalStorage.setStorageLocation("AUT");
    internalStorage.setBackupLocation("AUT");
    internalStorage.setActive(true);
    internalStorage.persist();

    InternalStorageTranslation internalStorageTranslation = new InternalStorageTranslation();
    internalStorageTranslation.setInternalStorageId(internalStorage);
    internalStorageTranslation.setTitle("Test Storage Title");
    internalStorageTranslation.setDescription(
        "Long winded yet brief description of the storage option");
    internalStorageTranslation.setLanguageCode("eng");
    internalStorageTranslation.persistAndFlush();

    return internalStorage.id;
  }

  @Transactional
  public List<Long> prepareInternalStorageTranslationOption(boolean generateTwo) {

    InternalStorage internalStorage = new InternalStorage();
    internalStorage.setUrl("test.url.com");
    internalStorage.setStorageLocation("AUT");
    internalStorage.setBackupLocation("AUT");
    internalStorage.setActive(true);
    internalStorage.persist();

    InternalStorageTranslation internalStorageTranslation = new InternalStorageTranslation();
    internalStorageTranslation.setInternalStorageId(internalStorage);
    internalStorageTranslation.setTitle("Test Storage Title ENG");
    internalStorageTranslation.setDescription(
        "Long winded yet brief description of the storage option");
    internalStorageTranslation.setLanguageCode("eng");
    internalStorageTranslation.persistAndFlush();

    if (generateTwo) {
      InternalStorageTranslation internalStorageTranslation2 = new InternalStorageTranslation();
      internalStorageTranslation2.setInternalStorageId(internalStorage);
      internalStorageTranslation2.setTitle("Test Storage Title DEU");
      internalStorageTranslation2.setDescription(
          "Long winded yet brief description of the storage option");
      internalStorageTranslation2.setLanguageCode("deu");
      internalStorageTranslation2.persistAndFlush();

      return List.of(
          internalStorage.id, internalStorageTranslation.id, internalStorageTranslation2.id);
    }

    return List.of(internalStorage.id, internalStorageTranslation.id);
  }

  /**
   * getOrCreateTestDmpDOInvalidData.
   *
   * @return a {@link org.damap.base.rest.dmp.domain.DmpDO} object
   */
  @Transactional
  public DmpDO getOrCreateTestDmpDOInvalidData() {
    DmpDO newInvalidTestDmpDO = getOrCreateTestDmpDO();
    newInvalidTestDmpDO.setTitle("MalformedTestDmp");

    newInvalidTestDmpDO.getDatasets().get(0).setLicense(ELicense.CCBY);
    newInvalidTestDmpDO.getCosts().get(0).setCurrencyCode("DOUBLOONS");
    newInvalidTestDmpDO.getRepositories().get(0).setRepositoryId("Arbitrary Host ID");
    newInvalidTestDmpDO.getExternalStorage().get(0).setUrl("Link to the storage service");

    return newInvalidTestDmpDO;
  }

  /**
   * getOrCreateTestVersionDO.
   *
   * @return a {@link org.damap.base.rest.version.VersionDO} object
   */
  public VersionDO getOrCreateTestVersionDO() {
    DmpDO dmpDO = getOrCreateTestDmpDO();

    final Optional<DmpVersion> testDmpVersion =
        dmpVersionRepo.getAll().stream()
            .filter(a -> a.getVersionName().equals("TestVersion"))
            .findAny();
    if (testDmpVersion.isPresent()) {
      return VersionDOMapper.mapEntityToDO(testDmpVersion.get(), new VersionDO());
    }

    VersionDO versionDO = new VersionDO();
    versionDO.setVersionName("TestVersion");
    versionDO.setDmpId(dmpDO.getId());

    versionService.createOrUpdate(versionDO);

    return getOrCreateTestVersionDO();
  }

  /**
   * getORCIDTestRecord.
   *
   * @return a {@link ORCIDRecord} object
   */
  public ORCIDRecord getORCIDTestRecord() {
    ORCIDRecord record = new ORCIDRecord();
    URL url = Resources.getResource("json/orcidRecord.json");
    try (InputStream in = url.openStream()) {
      ObjectMapper mapper = new ObjectMapper();
      record = mapper.readValue(in, ORCIDRecord.class);

    } catch (Exception e) {
      e.printStackTrace();
    }

    return record;
  }
}
