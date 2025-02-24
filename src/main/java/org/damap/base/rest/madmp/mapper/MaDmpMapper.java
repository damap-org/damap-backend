package org.damap.base.rest.madmp.mapper;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.enums.*;
import org.damap.base.rest.dmp.domain.*;
import org.damap.base.rest.dmp.mapper.MapperService;
import org.damap.base.rest.madmp.dto.*;
import org.damap.base.rest.storage.InternalStorageDO;
import org.damap.base.rest.storage.InternalStorageTranslationDO;
import org.re3data.schema._2_2.Certificates;
import org.re3data.schema._2_2.PidSystems;
import org.re3data.schema._2_2.Re3Data;
import org.re3data.schema._2_2.Yesno;

/** MaDmpMapper class. */
@JBossLog
@UtilityClass
public class MaDmpMapper {

  // TODO the language code should be sourced from the template that is being
  // exported
  static final String DEFAULT_LANGUAGE_CODE = "eng";

  /**
   * mapToMaDmp.
   *
   * @param dmpDO a {@link org.damap.base.rest.dmp.domain.DmpDO} object
   * @param dmp a {@link org.damap.base.rest.madmp.dto.Dmp} object
   * @param mapperService a {@link org.damap.base.rest.dmp.mapper.MapperService} object
   * @return a {@link org.damap.base.rest.madmp.dto.Dmp} object
   */
  public Dmp mapToMaDmp(DmpDO dmpDO, Dmp dmp, MapperService mapperService) {

    if (dmpDO.getContact() != null) dmp.setContact(mapToMaDmp(dmpDO.getContact(), new Contact()));

    List<Contributor> contributorList = new ArrayList<>();
    dmpDO
        .getContributors()
        .forEach(
            contributorDO -> contributorList.add(mapToMaDmp(contributorDO, new Contributor())));
    dmp.setContributor(contributorList);

    List<Cost> costList = new ArrayList<>();
    dmpDO.getCosts().forEach(costDO -> costList.add(mapToMaDmp(costDO, new Cost())));
    dmp.setCost(costList);

    dmp.setCreated(dmpDO.getCreated());

    List<Dataset> datasetList = new ArrayList<>();
    dmpDO
        .getDatasets()
        .forEach(
            datasetDO ->
                datasetList.add(mapToMaDmp(dmpDO, datasetDO, new Dataset(), mapperService)));
    dmp.setDataset(datasetList);

    dmp.setDescription(dmpDO.getDescription());
    dmp.setDmpId(null);
    dmp.setEthicalIssuesDescription(null);
    dmp.setEthicalIssuesExist(getEthicalIssuesExist(dmpDO));
    dmp.setEthicalIssuesReport(null);
    dmp.setLanguage(null);
    dmp.setModified(dmpDO.getModified());

    if (dmpDO.getProject() != null) {
      List<Project> projectList = new ArrayList<>();
      projectList.add(mapToMaDmp(dmpDO.getProject(), new Project()));
      dmp.setProject(projectList);
    }

    dmp.setTitle(dmpDO.getTitle());

    return dmp;
  }

  /**
   * mapToMaDmp.
   *
   * @param contactDO a {@link org.damap.base.rest.dmp.domain.ContributorDO} object
   * @param contact a {@link org.damap.base.rest.madmp.dto.Contact} object
   * @return a {@link org.damap.base.rest.madmp.dto.Contact} object
   */
  public Contact mapToMaDmp(ContributorDO contactDO, Contact contact) {

    if (contactDO.getPersonId() != null)
      contact.setContactId(mapToMaDmp(contactDO.getPersonId(), new ContactId()));
    contact.setMbox(contactDO.getMbox());
    contact.setName(contactDO.getFirstName() + " " + contactDO.getLastName());

    return contact;
  }

  /**
   * mapToMaDmp.
   *
   * @param identifierDO a {@link org.damap.base.rest.dmp.domain.IdentifierDO} object
   * @param contactId a {@link org.damap.base.rest.madmp.dto.ContactId} object
   * @return a {@link org.damap.base.rest.madmp.dto.ContactId} object
   */
  public ContactId mapToMaDmp(IdentifierDO identifierDO, ContactId contactId) {

    contactId.setIdentifier(identifierDO.getIdentifier());
    switch (identifierDO.getType()) {
      case ORCID:
        contactId.setType(ContactId.Type.ORCID);
        break;
      case ISNI:
        contactId.setType(ContactId.Type.ISNI);
        break;
      case OPENID:
        contactId.setType(ContactId.Type.OPENID);
        break;
      default:
        contactId.setType(ContactId.Type.OTHER);
    }
    return contactId;
  }

  /**
   * mapToMaDmp.
   *
   * @param contributorDO a {@link org.damap.base.rest.dmp.domain.ContributorDO} object
   * @param contributor a {@link org.damap.base.rest.madmp.dto.Contributor} object
   * @return a {@link org.damap.base.rest.madmp.dto.Contributor} object
   */
  public Contributor mapToMaDmp(ContributorDO contributorDO, Contributor contributor) {

    if (contributorDO.getPersonId() != null)
      contributor.setContributorId(mapToMaDmp(contributorDO.getPersonId(), new ContributorId()));

    contributor.setMbox(contributorDO.getMbox());
    contributor.setName(contributorDO.getFirstName() + " " + contributorDO.getLastName());

    Set<String> roles =
        contributorDO.getRoles() != null
            ? contributorDO.getRoles().stream()
                .map(EContributorRole::getRoles)
                .collect(Collectors.toSet())
            : new LinkedHashSet<>();
    contributor.setRole(roles);

    return contributor;
  }

  /**
   * mapToMaDmp.
   *
   * @param identifierDO a {@link org.damap.base.rest.dmp.domain.IdentifierDO} object
   * @param contributorId a {@link org.damap.base.rest.madmp.dto.ContributorId} object
   * @return a {@link org.damap.base.rest.madmp.dto.ContributorId} object
   */
  public ContributorId mapToMaDmp(IdentifierDO identifierDO, ContributorId contributorId) {

    contributorId.setIdentifier(identifierDO.getIdentifier());
    switch (identifierDO.getType()) {
      case ORCID:
        contributorId.setType(ContributorId.Type.ORCID);
        break;
      case ISNI:
        contributorId.setType(ContributorId.Type.ISNI);
        break;
      case OPENID:
        contributorId.setType(ContributorId.Type.OPENID);
        break;
      default:
        contributorId.setType(ContributorId.Type.OTHER);
    }
    return contributorId;
  }

  /**
   * mapToMaDmp.
   *
   * @param costDO a {@link org.damap.base.rest.dmp.domain.CostDO} object
   * @param cost a {@link org.damap.base.rest.madmp.dto.Cost} object
   * @return a {@link org.damap.base.rest.madmp.dto.Cost} object
   */
  public Cost mapToMaDmp(CostDO costDO, Cost cost) {

    if (costDO.getCurrencyCode() != null)
      cost.setCurrencyCode(Cost.CurrencyCode.fromValue(costDO.getCurrencyCode()));
    cost.setDescription(costDO.getDescription());
    cost.setTitle(costDO.getTitle());
    cost.setValue(cost.getValue());

    return cost;
  }

  /**
   * mapToMaDmp.
   *
   * @param dmpDO a {@link org.damap.base.rest.dmp.domain.DmpDO} object
   * @param datasetDO a {@link org.damap.base.rest.dmp.domain.DatasetDO} object
   * @param dataset a {@link org.damap.base.rest.madmp.dto.Dataset} object
   * @param mapperService a {@link org.damap.base.rest.dmp.mapper.MapperService} object
   * @return a {@link org.damap.base.rest.madmp.dto.Dataset} object
   */
  public Dataset mapToMaDmp(
      DmpDO dmpDO, DatasetDO datasetDO, Dataset dataset, MapperService mapperService) {

    dataset.setDataQualityAssurance(null);
    dataset.setDatasetId(null);
    dataset.setDescription(datasetDO.getDescription());

    List<Distribution> distributionList = new ArrayList<>();
    dmpDO.getRepositories().stream()
        .filter(hostDO -> hostDO.getDatasets().contains(datasetDO.getReferenceHash()))
        .forEach(
            repositoryDO -> {
              Distribution distribution = mapToMaDmp(datasetDO, new Distribution());
              distributionList.add(
                  mapToMaDmpFromRepository(repositoryDO, distribution, mapperService));
            });
    dmpDO.getStorage().stream()
        .filter(hostDO -> hostDO.getDatasets().contains(datasetDO.getReferenceHash()))
        .forEach(
            storageDO -> {
              Distribution distribution = mapToMaDmp(datasetDO, new Distribution());
              distributionList.add(mapToMaDmpFromStorage(storageDO, distribution, mapperService));
            });
    dmpDO.getExternalStorage().stream()
        .filter(hostDO -> hostDO.getDatasets().contains(datasetDO.getReferenceHash()))
        .forEach(
            externalStorageDO -> {
              Distribution distribution = mapToMaDmp(datasetDO, new Distribution());
              distributionList.add(mapToMaDmpFromExternalStorage(externalStorageDO, distribution));
            });
    dataset.setDistribution(distributionList);

    dataset.setIssued(null);
    dataset.setKeyword(null);
    dataset.setLanguage(null);
    dataset.setMetadata(null);
    dataset.setPersonalData(getPersonalData(datasetDO));
    dataset.setPreservationStatement(null);
    dataset.setSecurityAndPrivacy(getSecurityAndPrivacyList(dmpDO, datasetDO));
    dataset.setSensitiveData(getSensitiveData(datasetDO));
    dataset.setTechnicalResource(null);
    dataset.setTitle(datasetDO.getTitle());
    dataset.setType(mapToMaDmpDatasetType(datasetDO.getType()));

    dataset.setTechnicalResource(getTechnicalResource(datasetDO));

    return dataset;
  }

  private List<TechnicalResource> getTechnicalResource(DatasetDO datasetDO) {
    List<TechnicalResource> technicalResources = new ArrayList<>();
    for (TechnicalResourceDO technicalResourceDO : datasetDO.getTechnicalResources()) {
      TechnicalResource technicalResource = new TechnicalResource();
      technicalResource.setName(technicalResourceDO.getName());
      // We need to check if the description is null, because the description is not mandatory
      // In case it is null, we set it to an empty string so that the field is included in the JSON
      if (technicalResourceDO.getDescription() == null) {
        technicalResource.setDescription("");
      } else {
        technicalResource.setDescription(technicalResourceDO.getDescription());
      }
      technicalResources.add(technicalResource);
    }
    return technicalResources;
  }

  /**
   * mapToMaDmpDatasetType.
   *
   * @param types a {@link java.util.List} object
   * @return a {@link java.lang.String} object
   */
  public String mapToMaDmpDatasetType(List<EDataType> types) {
    if (types == null || types.isEmpty()) {
      return "";
    }
    return types.stream().map(EDataType::getValue).collect(Collectors.joining(", "));
  }

  /**
   * mapToMaDmp.
   *
   * @param datasetDO a {@link org.damap.base.rest.dmp.domain.DatasetDO} object
   * @param distribution a {@link org.damap.base.rest.madmp.dto.Distribution} object
   * @return a {@link org.damap.base.rest.madmp.dto.Distribution} object
   */
  public Distribution mapToMaDmp(DatasetDO datasetDO, Distribution distribution) {

    if (datasetDO.getDateOfDeletion() != null)
      distribution.setAvailableUntil(datasetDO.getDateOfDeletion().toString());
    if (datasetDO.getSize() != null) distribution.setByteSize(datasetDO.getSize().intValue());
    if (datasetDO.getDataAccess() != null)
      distribution.setDataAccess(getDataAccess(datasetDO.getDataAccess()));
    distribution.setDescription(datasetDO.getDescription());
    distribution.setDownloadUrl(null);
    if (datasetDO.getType() != null)
      distribution.setFormat(mapToMaDmpDatasetFormat(datasetDO.getType()));
    if (datasetDO.getLicense() != null)
      distribution.setLicense(List.of(mapToMaDmp(datasetDO, new License())));
    distribution.setTitle(datasetDO.getTitle());
    return distribution;
  }

  /**
   * mapToMaDmpDatasetFormat.
   *
   * @param types a {@link java.util.List} object
   * @return a {@link java.util.List} object
   */
  public List<String> mapToMaDmpDatasetFormat(List<EDataType> types) {
    if (types == null || types.isEmpty()) {
      return new ArrayList<>();
    }
    return types.stream().map(EDataType::getValue).collect(Collectors.toList());
  }

  /**
   * mapToMaDmpFromRepository.
   *
   * @param repositoryDO a {@link org.damap.base.rest.dmp.domain.RepositoryDO} object
   * @param distribution a {@link org.damap.base.rest.madmp.dto.Distribution} object
   * @param mapperService a {@link org.damap.base.rest.dmp.mapper.MapperService} object
   * @return a {@link org.damap.base.rest.madmp.dto.Distribution} object
   */
  public Distribution mapToMaDmpFromRepository(
      RepositoryDO repositoryDO, Distribution distribution, MapperService mapperService) {

    Re3Data.Repository repository =
        mapperService.getRe3DataRepository(repositoryDO.getRepositoryId());
    distribution.setAccessUrl(repository.getRepositoryURL());
    distribution.setHost(mapToMaDmpFromRepository(repository, new Host()));
    return distribution;
  }

  /**
   * mapToMaDmpFromStorage.
   *
   * @param storageDO a {@link org.damap.base.rest.dmp.domain.StorageDO} object
   * @param distribution a {@link org.damap.base.rest.madmp.dto.Distribution} object
   * @param mapperService a {@link org.damap.base.rest.dmp.mapper.MapperService} object
   * @return a {@link org.damap.base.rest.madmp.dto.Distribution} object
   */
  public Distribution mapToMaDmpFromStorage(
      StorageDO storageDO, Distribution distribution, MapperService mapperService) {

    InternalStorageDO internalStorageDO =
        mapperService.getInternalStorageDOById(
            storageDO.getInternalStorageId(), DEFAULT_LANGUAGE_CODE);
    if (internalStorageDO != null) {
      distribution.setAccessUrl(internalStorageDO.getUrl());
      distribution.setHost(mapToMaDmpFromInternalStorage(internalStorageDO, new Host()));
    }
    return distribution;
  }

  /**
   * mapToMaDmpFromExternalStorage.
   *
   * @param externalStorageDO a {@link org.damap.base.rest.dmp.domain.ExternalStorageDO} object
   * @param distribution a {@link org.damap.base.rest.madmp.dto.Distribution} object
   * @return a {@link org.damap.base.rest.madmp.dto.Distribution} object
   */
  public Distribution mapToMaDmpFromExternalStorage(
      ExternalStorageDO externalStorageDO, Distribution distribution) {

    distribution.setAccessUrl(externalStorageDO.getUrl());
    distribution.setHost(mapToMaDmpFromExternalStorage(externalStorageDO, new Host()));
    return distribution;
  }

  /**
   * getDataAccess.
   *
   * @param eDataAccessType a {@link org.damap.base.enums.EDataAccessType} object
   * @return a {@link org.damap.base.rest.madmp.dto.Distribution.DataAccess} object
   */
  public Distribution.DataAccess getDataAccess(EDataAccessType eDataAccessType) {
    switch (eDataAccessType) {
      case CLOSED:
        return Distribution.DataAccess.CLOSED;
      case RESTRICTED:
        return Distribution.DataAccess.SHARED;
      case OPEN:
      default:
        return Distribution.DataAccess.OPEN;
    }
  }

  /**
   * mapToMaDmpFromRepository.
   *
   * @param repository a {@link org.re3data.schema._2_2.Re3Data.Repository} object
   * @param host a {@link org.damap.base.rest.madmp.dto.Host} object
   * @return a {@link org.damap.base.rest.madmp.dto.Host} object
   */
  public Host mapToMaDmpFromRepository(Re3Data.Repository repository, Host host) {

    host.setAvailability(null);
    host.setBackupFrequency(null);
    host.setBackupType(null);
    host.setCertifiedWith(getCertifiedWith(repository.getCertificate()));
    if (repository.getDescription() != null)
      host.setDescription(repository.getDescription().getValue());
    host.setGeoLocation(null);

    List<PidSystem> pidSystemList = new ArrayList<>();
    repository
        .getPidSystem()
        .forEach(repoPidSystems -> pidSystemList.add(getPidSystem(repoPidSystems)));
    host.setPidSystem(pidSystemList);
    repository.getType().stream()
        .findFirst()
        .ifPresent(repositoryTypes -> host.setStorageType(repositoryTypes.value()));
    host.setSupportVersioning(getSupportVersioning(repository.getVersioning()));
    if (repository.getRepositoryName() != null)
      host.setTitle(repository.getRepositoryName().getValue());
    if (repository.getRepositoryURL() != null)
      host.setUrl(URI.create(repository.getRepositoryURL()));
    return host;
  }

  /**
   * getCertifiedWith.
   *
   * @param certificates a {@link java.util.List} object
   * @return a {@link org.damap.base.rest.madmp.dto.Host.CertifiedWith} object
   */
  public Host.CertifiedWith getCertifiedWith(List<Certificates> certificates) {
    // This currently returns the very first available certificate it can map to.
    for (Certificates certificate : certificates) {
      switch (certificate) {
        case DIN_31644:
          return Host.CertifiedWith.DIN_31644;
        case DINI_CERTIFICATE:
          return Host.CertifiedWith.DINI_ZERTIFIKAT;
        case DSA:
          return Host.CertifiedWith.DSA;
        case ISO_16363:
          return Host.CertifiedWith.ISO_16363;
        case ISO_16919:
          return Host.CertifiedWith.ISO_16919;
        case TRAC:
          return Host.CertifiedWith.TRAC;
        case WDS:
          return Host.CertifiedWith.WDS;
          // case XXX: return Host.CertifiedWith.CORETRUSTSEAL;
        case CLARIN_CERTIFICATE_B:
        case DRAMBORA:
        case RAT_SWD:
        case TRUSTED_DIGITAL_REPOSITORY:
        case OTHER:
      }
    }
    return null;
  }

  /**
   * getPidSystem.
   *
   * @param pidSystems a {@link org.re3data.schema._2_2.PidSystems} object
   * @return a {@link org.damap.base.rest.madmp.dto.PidSystem} object
   */
  public PidSystem getPidSystem(PidSystems pidSystems) {
    switch (pidSystems) {
      case ARK:
        return PidSystem.ARK;
      case DOI:
        return PidSystem.DOI;
      case HDL:
        return PidSystem.HANDLE;
      case PURL:
        return PidSystem.PURL;
      case URN:
        return PidSystem.URN;
      case OTHER:
        return PidSystem.OTHER;
      case NONE:
      default:
        return null;
    }
  }

  /**
   * getSupportVersioning.
   *
   * @param versioning a {@link org.re3data.schema._2_2.Yesno} object
   * @return a {@link org.damap.base.rest.madmp.dto.Host.SupportVersioning} object
   */
  public Host.SupportVersioning getSupportVersioning(Yesno versioning) {
    if (versioning == Yesno.YES) return Host.SupportVersioning.YES;
    if (versioning == Yesno.NO) return Host.SupportVersioning.NO;
    return Host.SupportVersioning.UNKNOWN;
  }

  /**
   * mapToMaDmpFromInternalStorage.
   *
   * @param internalStorageDO a {@link org.damap.base.rest.storage.InternalStorageDO} object
   * @param host a {@link org.damap.base.rest.madmp.dto.Host} object
   * @return a {@link org.damap.base.rest.madmp.dto.Host} object
   */
  public Host mapToMaDmpFromInternalStorage(InternalStorageDO internalStorageDO, Host host) {

    host.setAvailability(null);
    host.setBackupType(null);
    host.setCertifiedWith(null);

    host.setGeoLocation(null);
    host.setPidSystem(null);
    host.setStorageType(null);
    host.setSupportVersioning(null);

    List<InternalStorageTranslationDO> translations = internalStorageDO.getTranslations();
    InternalStorageTranslationDO internalStorageTranslationDO = null;

    if (!translations.isEmpty()) {
      internalStorageTranslationDO = translations.get(0);
    }

    for (InternalStorageTranslationDO translationDO : translations) {
      if (translationDO.getLanguageCode().equals(DEFAULT_LANGUAGE_CODE)) {
        internalStorageTranslationDO = translationDO;
        break;
      }
    }

    host.setTitle(internalStorageTranslationDO.getTitle());
    host.setDescription(internalStorageTranslationDO.getDescription());
    host.setBackupFrequency(internalStorageTranslationDO.getBackupFrequency());

    if (internalStorageDO.getUrl() != null) host.setUrl(URI.create(internalStorageDO.getUrl()));
    return host;
  }

  /**
   * mapToMaDmpFromExternalStorage.
   *
   * @param externalStorageDO a {@link org.damap.base.rest.dmp.domain.ExternalStorageDO} object
   * @param host a {@link org.damap.base.rest.madmp.dto.Host} object
   * @return a {@link org.damap.base.rest.madmp.dto.Host} object
   */
  public Host mapToMaDmpFromExternalStorage(ExternalStorageDO externalStorageDO, Host host) {

    host.setAvailability(null);
    host.setBackupFrequency(externalStorageDO.getBackupFrequency());
    host.setBackupType(null);
    host.setCertifiedWith(null);
    host.setDescription(null);
    host.setGeoLocation(null);
    host.setPidSystem(null);
    host.setStorageType(null);
    host.setSupportVersioning(null);
    host.setTitle(null);
    if (externalStorageDO.getUrl() != null) host.setUrl(URI.create(externalStorageDO.getUrl()));
    return host;
  }

  /**
   * mapToMaDmp.
   *
   * @param datasetDO a {@link org.damap.base.rest.dmp.domain.DatasetDO} object
   * @param license a {@link org.damap.base.rest.madmp.dto.License} object
   * @return a {@link org.damap.base.rest.madmp.dto.License} object
   */
  public License mapToMaDmp(DatasetDO datasetDO, License license) {

    if (datasetDO.getLicense() != null)
      license.setLicenseRef(URI.create(datasetDO.getLicense().getUrl()));
    if (datasetDO.getStartDate() != null) license.setStartDate(datasetDO.getStartDate().toString());
    return license;
  }

  /**
   * getPersonalData.
   *
   * @param datasetDO a {@link org.damap.base.rest.dmp.domain.DatasetDO} object
   * @return a {@link org.damap.base.rest.madmp.dto.Dataset.PersonalData} object
   */
  public Dataset.PersonalData getPersonalData(DatasetDO datasetDO) {

    if (Boolean.TRUE.equals(datasetDO.getPersonalData())) return Dataset.PersonalData.YES;
    if (Boolean.FALSE.equals(datasetDO.getPersonalData())) return Dataset.PersonalData.NO;
    return Dataset.PersonalData.UNKNOWN;
  }

  /**
   * getSecurityAndPrivacyList.
   *
   * @param dmpDO a {@link org.damap.base.rest.dmp.domain.DmpDO} object
   * @param datasetDO a {@link org.damap.base.rest.dmp.domain.DatasetDO} object
   * @return a {@link java.util.List} object
   */
  public List<SecurityAndPrivacy> getSecurityAndPrivacyList(DmpDO dmpDO, DatasetDO datasetDO) {

    List<SecurityAndPrivacy> securityAndPrivacyList = new ArrayList<>();

    if (datasetDO.getSensitiveData() != null && datasetDO.getSensitiveData()) {
      dmpDO
          .getSensitiveDataSecurity()
          .forEach(
              eSecurityMeasure ->
                  securityAndPrivacyList.add(
                      mapToMaDmp(eSecurityMeasure, new SecurityAndPrivacy())));
    }
    if (datasetDO.getPersonalData() != null && datasetDO.getPersonalData()) {
      dmpDO
          .getPersonalDataCompliance()
          .forEach(
              eComplianceType ->
                  securityAndPrivacyList.add(
                      mapToMaDmp(eComplianceType, new SecurityAndPrivacy())));
    }
    if (datasetDO.getLegalRestrictions() != null && datasetDO.getLegalRestrictions()) {
      dmpDO
          .getLegalRestrictionsDocuments()
          .forEach(
              eAgreement ->
                  securityAndPrivacyList.add(mapToMaDmp(eAgreement, new SecurityAndPrivacy())));
    }
    return securityAndPrivacyList;
  }

  /**
   * mapToMaDmp.
   *
   * @param eSecurityMeasure a {@link org.damap.base.enums.ESecurityMeasure} object
   * @param securityAndPrivacy a {@link org.damap.base.rest.madmp.dto.SecurityAndPrivacy} object
   * @return a {@link org.damap.base.rest.madmp.dto.SecurityAndPrivacy} object
   */
  public SecurityAndPrivacy mapToMaDmp(
      ESecurityMeasure eSecurityMeasure, SecurityAndPrivacy securityAndPrivacy) {

    securityAndPrivacy.setDescription(eSecurityMeasure.getValue());
    securityAndPrivacy.setTitle(eSecurityMeasure.name());
    return securityAndPrivacy;
  }

  /**
   * mapToMaDmp.
   *
   * @param eComplianceType a {@link org.damap.base.enums.EComplianceType} object
   * @param securityAndPrivacy a {@link org.damap.base.rest.madmp.dto.SecurityAndPrivacy} object
   * @return a {@link org.damap.base.rest.madmp.dto.SecurityAndPrivacy} object
   */
  public SecurityAndPrivacy mapToMaDmp(
      EComplianceType eComplianceType, SecurityAndPrivacy securityAndPrivacy) {

    securityAndPrivacy.setDescription(eComplianceType.getValue());
    securityAndPrivacy.setTitle(eComplianceType.name());
    return securityAndPrivacy;
  }

  /**
   * mapToMaDmp.
   *
   * @param eAgreement a {@link org.damap.base.enums.EAgreement} object
   * @param securityAndPrivacy a {@link org.damap.base.rest.madmp.dto.SecurityAndPrivacy} object
   * @return a {@link org.damap.base.rest.madmp.dto.SecurityAndPrivacy} object
   */
  public SecurityAndPrivacy mapToMaDmp(
      EAgreement eAgreement, SecurityAndPrivacy securityAndPrivacy) {

    securityAndPrivacy.setDescription(eAgreement.getValue());
    securityAndPrivacy.setTitle(eAgreement.name());
    return securityAndPrivacy;
  }

  /**
   * getSensitiveData.
   *
   * @param datasetDO a {@link org.damap.base.rest.dmp.domain.DatasetDO} object
   * @return a {@link org.damap.base.rest.madmp.dto.Dataset.SensitiveData} object
   */
  public Dataset.SensitiveData getSensitiveData(DatasetDO datasetDO) {

    if (Boolean.TRUE.equals(datasetDO.getSensitiveData())) return Dataset.SensitiveData.YES;
    if (Boolean.FALSE.equals(datasetDO.getSensitiveData())) return Dataset.SensitiveData.NO;
    return Dataset.SensitiveData.UNKNOWN;
  }

  /**
   * getEthicalIssuesExist.
   *
   * @param dmpDO a {@link org.damap.base.rest.dmp.domain.DmpDO} object
   * @return a {@link org.damap.base.rest.madmp.dto.Dmp.EthicalIssuesExist} object
   */
  public Dmp.EthicalIssuesExist getEthicalIssuesExist(DmpDO dmpDO) {

    if (Boolean.TRUE.equals(dmpDO.getEthicalIssuesExist())
        || Boolean.TRUE.equals(dmpDO.getHumanParticipants())) return Dmp.EthicalIssuesExist.YES;
    if (Boolean.FALSE.equals(dmpDO.getEthicalIssuesExist())
        || Boolean.FALSE.equals(dmpDO.getHumanParticipants())) return Dmp.EthicalIssuesExist.NO;
    return Dmp.EthicalIssuesExist.UNKNOWN;
  }

  /**
   * mapToMaDmp.
   *
   * @param projectDO a {@link org.damap.base.rest.dmp.domain.ProjectDO} object
   * @param project a {@link org.damap.base.rest.madmp.dto.Project} object
   * @return a {@link org.damap.base.rest.madmp.dto.Project} object
   */
  public Project mapToMaDmp(ProjectDO projectDO, Project project) {

    project.setDescription(projectDO.getDescription());
    if (projectDO.getEnd() != null) project.setEnd(projectDO.getEnd().toString());

    if (projectDO.getFunding() != null) {
      List<Funding> fundingList = new ArrayList<>();
      fundingList.add(mapToMaDmp(projectDO.getFunding(), new Funding()));
      project.setFunding(fundingList);
    }

    if (projectDO.getStart() != null) project.setStart(projectDO.getStart().toString());
    project.setTitle(projectDO.getTitle());

    return project;
  }

  /**
   * mapToMaDmp.
   *
   * @param fundingDO a {@link org.damap.base.rest.dmp.domain.FundingDO} object
   * @param funding a {@link org.damap.base.rest.madmp.dto.Funding} object
   * @return a {@link org.damap.base.rest.madmp.dto.Funding} object
   */
  public Funding mapToMaDmp(FundingDO fundingDO, Funding funding) {

    if (fundingDO.getFunderId() != null)
      funding.setFunderId(mapToMaDmp(fundingDO.getFunderId(), new FunderId()));
    switch (fundingDO.getFundingStatus()) {
      case PLANNED:
        funding.setFundingStatus(Funding.FundingStatus.PLANNED);
        break;
      case APPLIED:
        funding.setFundingStatus(Funding.FundingStatus.APPLIED);
        break;
      case GRANTED:
        funding.setFundingStatus(Funding.FundingStatus.GRANTED);
        break;
      case REJECTED:
        funding.setFundingStatus(Funding.FundingStatus.REJECTED);
        break;
      case UNSPECIFIED:
      default:
        funding.setFundingStatus(null);
    }
    if (fundingDO.getGrantId() != null)
      funding.setGrantId(mapToMaDmp(fundingDO.getGrantId(), new GrantId()));

    return funding;
  }

  /**
   * mapToMaDmp.
   *
   * @param identifierDO a {@link org.damap.base.rest.dmp.domain.IdentifierDO} object
   * @param funderId a {@link org.damap.base.rest.madmp.dto.FunderId} object
   * @return a {@link org.damap.base.rest.madmp.dto.FunderId} object
   */
  public FunderId mapToMaDmp(IdentifierDO identifierDO, FunderId funderId) {

    funderId.setIdentifier(identifierDO.getIdentifier());
    if (identifierDO.getType() != null) {
      switch (identifierDO.getType()) {
        case FUNDREF:
          funderId.setType(FunderId.Type.FUNDREF);
          break;
        case URL:
          funderId.setType(FunderId.Type.URL);
          break;
        default:
          funderId.setType(FunderId.Type.OTHER);
      }
    }
    return funderId;
  }

  /**
   * mapToMaDmp.
   *
   * @param identifierDO a {@link org.damap.base.rest.dmp.domain.IdentifierDO} object
   * @param grantId a {@link org.damap.base.rest.madmp.dto.GrantId} object
   * @return a {@link org.damap.base.rest.madmp.dto.GrantId} object
   */
  public GrantId mapToMaDmp(IdentifierDO identifierDO, GrantId grantId) {

    grantId.setIdentifier(identifierDO.getIdentifier());
    if (identifierDO.getType() != null) {
      switch (identifierDO.getType()) {
        case URL:
          grantId.setType(GrantId.Type.URL);
          break;
        default:
          grantId.setType(GrantId.Type.OTHER);
      }
    }
    return grantId;
  }
}
