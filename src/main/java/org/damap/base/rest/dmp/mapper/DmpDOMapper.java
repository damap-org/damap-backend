package org.damap.base.rest.dmp.mapper;

import java.util.*;
import lombok.experimental.UtilityClass;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.domain.*;
import org.damap.base.enums.EAgreement;
import org.damap.base.enums.EComplianceType;
import org.damap.base.enums.EDataQualityType;
import org.damap.base.enums.ESecurityMeasure;
import org.damap.base.rest.dmp.domain.*;

/** DmpDOMapper class. */
@UtilityClass
@JBossLog
public class DmpDOMapper {

  /**
   * mapEntityToDO.
   *
   * @param dmp a {@link org.damap.base.domain.Dmp} object
   * @param dmpDO a {@link org.damap.base.rest.dmp.domain.DmpDO} object
   * @return a {@link org.damap.base.rest.dmp.domain.DmpDO} object
   */
  public DmpDO mapEntityToDO(Dmp dmp, DmpDO dmpDO) {
    dmpDO.setId(dmp.id);
    dmpDO.setTitle(dmp.getTitle());
    dmpDO.setCreated(dmp.getCreated());
    dmpDO.setModified(dmp.getModified());
    dmpDO.setDescription(dmp.getDescription());

    if (dmp.getProject() != null) {
      ProjectDO projectDO = new ProjectDO();
      ProjectDOMapper.mapEntityToDO(dmp.getProject(), projectDO);
      dmpDO.setProject(projectDO);
    }

    dmpDO.setDataKind(dmp.getDataKind());
    dmpDO.setReusedDataKind(dmp.getReusedDataKind());
    dmpDO.setNoDataExplanation(dmp.getNoDataExplanation());
    dmpDO.setMetadata(dmp.getMetadata());
    dmpDO.setDataGeneration(dmp.getDataGeneration());
    dmpDO.setStructure(dmp.getStructure());
    dmpDO.setOtherDataQuality(dmp.getOtherDataQuality());
    dmpDO.setTargetAudience(dmp.getTargetAudience());
    dmpDO.setTools(dmp.getTools());
    dmpDO.setRestrictedDataAccess(dmp.getRestrictedDataAccess());
    dmpDO.setPersonalData(dmp.getPersonalData());
    dmpDO.setPersonalDataCris(dmp.getPersonalDataCris());
    dmpDO.setOtherPersonalDataCompliance(dmp.getOtherPersonalDataCompliance());
    dmpDO.setSensitiveData(dmp.getSensitiveData());
    dmpDO.setSensitiveDataCris(dmp.getSensitiveDataCris());
    dmpDO.setOtherDataSecurityMeasures(dmp.getOtherDataSecurityMeasures());
    dmpDO.setSensitiveDataAccess(dmp.getSensitiveDataAccess());
    dmpDO.setLegalRestrictions(dmp.getLegalRestrictions());
    dmpDO.setLegalRestrictionsCris(dmp.getLegalRestrictionsCris());
    dmpDO.setOtherLegalRestrictionsDocument(dmp.getOtherLegalRestrictionsDocument());
    dmpDO.setLegalRestrictionsComment(dmp.getLegalRestrictionsComment());
    dmpDO.setDataRightsAndAccessControl(dmp.getDataRightsAndAccessControl());
    dmpDO.setHumanParticipants(dmp.getHumanParticipants());
    dmpDO.setHumanParticipantsCris(dmp.getHumanParticipantsCris());
    dmpDO.setEthicalIssuesExist(dmp.getEthicalIssuesExist());
    dmpDO.setEthicalIssuesExistCris(dmp.getEthicalIssuesExistCris());
    dmpDO.setCommitteeReviewed(dmp.getCommitteeReviewed());
    dmpDO.setCommitteeReviewedCris(dmp.getCommitteeReviewedCris());
    dmpDO.setExternalStorageInfo(dmp.getExternalStorageInfo());
    dmpDO.setRestrictedAccessInfo(dmp.getRestrictedAccessInfo());
    dmpDO.setClosedAccessInfo(dmp.getClosedAccessInfo());
    dmpDO.setCostsExist(dmp.getCostsExist());
    dmpDO.setCostsExistCris(dmp.getCostsExistCris());
    dmpDO.setDocumentation(dmp.getDocumentation());
    dmpDO.setEthicalIssuesReport(dmp.getEthicalIssuesReport());

    List<ContributorDO> contributorDOList = new ArrayList<>();
    dmp.getContributorList()
        .forEach(
            contributor -> {
              ContributorDO contributorDO = new ContributorDO();
              ContributorDOMapper.mapEntityToDO(contributor, contributorDO);
              contributorDOList.add(contributorDO);
            });
    dmpDO.setContributors(contributorDOList);

    List<EDataQualityType> dataQualityTypeDOList = new ArrayList<>(dmp.getDataQuality());
    dmpDO.setDataQuality(dataQualityTypeDOList);

    List<EComplianceType> personalDataComplianceDOList =
        new ArrayList<>(dmp.getPersonalDataCompliance());
    dmpDO.setPersonalDataCompliance(personalDataComplianceDOList);

    List<ESecurityMeasure> sensitiveDataSecurityDOList =
        new ArrayList<>(dmp.getSensitiveDataSecurity());
    dmpDO.setSensitiveDataSecurity(sensitiveDataSecurityDOList);

    List<EAgreement> legalRestrictionsDocumentsDOList =
        new ArrayList<>(dmp.getLegalRestrictionsDocuments());
    dmpDO.setLegalRestrictionsDocuments(legalRestrictionsDocumentsDOList);

    List<DatasetDO> datasetDOList = new ArrayList<>();
    dmp.getDatasetList()
        .forEach(
            dataset -> {
              DatasetDO datasetDO = new DatasetDO();
              DatasetDOMapper.mapEntityToDO(dataset, datasetDO);
              datasetDOList.add(datasetDO);
            });
    dmpDO.setDatasets(datasetDOList);

    List<RepositoryDO> repositoryDOList = new ArrayList<>();
    List<StorageDO> storageDOList = new ArrayList<>();
    List<ExternalStorageDO> externalStorageDOList = new ArrayList<>();
    dmp.getHostList()
        .forEach(
            host -> {
              HostDO hostDO = null;

              Class<? extends Host> hostClass = host.getClass();
              if (Repository.class.isAssignableFrom(hostClass)) {
                hostDO = new RepositoryDO();
                HostDOMapper.mapEntityToDO(host, hostDO);
                RepositoryDOMapper.mapEntityToDO((Repository) host, (RepositoryDO) hostDO);
                repositoryDOList.add((RepositoryDO) hostDO);
              } else if (Storage.class.isAssignableFrom(hostClass)) {
                hostDO = new StorageDO();
                HostDOMapper.mapEntityToDO(host, hostDO);
                StorageDOMapper.mapEntityToDO((Storage) host, (StorageDO) hostDO);
                storageDOList.add((StorageDO) hostDO);
              } else if (ExternalStorage.class.isAssignableFrom(hostClass)) {
                hostDO = new ExternalStorageDO();
                HostDOMapper.mapEntityToDO(host, hostDO);
                ExternalStorageDOMapper.mapEntityToDO(
                    (ExternalStorage) host, (ExternalStorageDO) hostDO);
                externalStorageDOList.add((ExternalStorageDO) hostDO);
              } else {
                log.warn(String.format("Could not infer host type from class %s", hostClass));
                return;
              }

              // add frontend referenceHash list to host
              List<String> referenceHashList = new ArrayList<>();
              host.getDistributionList()
                  .forEach(
                      distribution ->
                          referenceHashList.add(distribution.getDataset().getReferenceHash()));

              if (!referenceHashList.isEmpty()) {
                hostDO.setDatasets(referenceHashList);
              }
            });
    dmpDO.setRepositories(repositoryDOList);
    dmpDO.setStorage(storageDOList);
    dmpDO.setExternalStorage(externalStorageDOList);

    List<CostDO> costDOList = new ArrayList<>();
    dmp.getCosts()
        .forEach(
            cost -> {
              CostDO costDO = new CostDO();
              CostDOMapper.mapEntityToDO(cost, costDO);
              costDOList.add(costDO);
            });
    dmpDO.setCosts(costDOList);

    return dmpDO;
  }

  /**
   * mapDOtoEntity.
   *
   * @param dmpDO a {@link org.damap.base.rest.dmp.domain.DmpDO} object
   * @param dmp a {@link org.damap.base.domain.Dmp} object
   * @param mapperService a {@link org.damap.base.rest.dmp.mapper.MapperService} object
   * @return a {@link org.damap.base.domain.Dmp} object
   */
  public Dmp mapDOtoEntity(DmpDO dmpDO, Dmp dmp, MapperService mapperService) {

    if (dmpDO.getId() != null) dmp.id = dmpDO.getId();
    dmp.setTitle(dmpDO.getTitle());
    dmp.setDescription(dmpDO.getDescription());

    if (dmpDO.getProject() != null) {
      Project project = new Project();
      if (dmp.getProject() != null) project = dmp.getProject();
      ProjectDOMapper.mapDOtoEntity(dmpDO.getProject(), project);
      dmp.setProject(project);
    } else dmp.setProject(null);

    dmp.setDataKind(dmpDO.getDataKind());
    dmp.setReusedDataKind(dmpDO.getReusedDataKind());
    dmp.setNoDataExplanation(dmpDO.getNoDataExplanation());
    dmp.setMetadata(dmpDO.getMetadata());
    dmp.setDataGeneration(dmpDO.getDataGeneration());
    dmp.setStructure(dmpDO.getStructure());
    dmp.setOtherDataQuality(dmpDO.getOtherDataQuality());
    dmp.setTargetAudience(dmpDO.getTargetAudience());
    dmp.setTools(dmpDO.getTools());
    dmp.setRestrictedDataAccess(dmpDO.getRestrictedDataAccess());
    dmp.setPersonalData(dmpDO.getPersonalData());
    dmp.setPersonalDataCris(dmpDO.getPersonalDataCris());
    dmp.setOtherPersonalDataCompliance(dmpDO.getOtherPersonalDataCompliance());
    dmp.setSensitiveData(dmpDO.getSensitiveData());
    dmp.setSensitiveDataCris(dmpDO.getSensitiveDataCris());
    dmp.setOtherDataSecurityMeasures(dmpDO.getOtherDataSecurityMeasures());
    dmp.setSensitiveDataAccess(dmpDO.getSensitiveDataAccess());
    dmp.setLegalRestrictions(dmpDO.getLegalRestrictions());
    dmp.setLegalRestrictionsCris(dmpDO.getLegalRestrictionsCris());
    dmp.setOtherLegalRestrictionsDocument(dmpDO.getOtherLegalRestrictionsDocument());
    dmp.setLegalRestrictionsComment(dmpDO.getLegalRestrictionsComment());
    dmp.setDataRightsAndAccessControl(dmpDO.getDataRightsAndAccessControl());
    dmp.setHumanParticipants(dmpDO.getHumanParticipants());
    dmp.setHumanParticipantsCris(dmpDO.getHumanParticipantsCris());
    dmp.setEthicalIssuesExist(dmpDO.getEthicalIssuesExist());
    dmp.setEthicalIssuesExistCris(dmpDO.getEthicalIssuesExistCris());
    dmp.setCommitteeReviewed(dmpDO.getCommitteeReviewed());
    dmp.setCommitteeReviewedCris(dmpDO.getCommitteeReviewedCris());
    dmp.setExternalStorageInfo(dmpDO.getExternalStorageInfo());
    dmp.setRestrictedAccessInfo(dmpDO.getRestrictedAccessInfo());
    dmp.setClosedAccessInfo(dmpDO.getClosedAccessInfo());
    dmp.setCostsExist(dmpDO.getCostsExist());
    dmp.setCostsExistCris(dmpDO.getCostsExistCris());
    dmp.setDocumentation(dmpDO.getDocumentation());
    dmp.setEthicalIssuesReport(dmpDO.getEthicalIssuesReport());

    // TODO also check for existing contributors based on Identifier, not just
    // universityId

    // remove all existing Contributor objects, that are not included in the DO
    // anymore
    List<Contributor> contributorList = dmp.getContributorList();
    List<Contributor> contributorListToRemove = new ArrayList<>();
    contributorList.forEach(
        contributor -> {
          Optional<ContributorDO> contributorDOOptional =
              dmpDO.getContributors().stream()
                  .filter(
                      contributorDO ->
                          contributorDO.getId() != null
                              && contributorDO.getId().equals(contributor.id))
                  .findFirst();
          if (contributorDOOptional.isEmpty()) {
            contributorListToRemove.add(contributor);
          }
        });
    contributorList.removeAll(contributorListToRemove);

    List<EDataQualityType> dataQualityTypeList = new ArrayList<>();
    dmpDO
        .getDataQuality()
        .forEach(
            option -> {
              if (option != null) {
                dataQualityTypeList.add(option);
              }
            });
    dmp.setDataQuality(dataQualityTypeList);

    List<EComplianceType> personalDataComplianceList = new ArrayList<>();
    dmpDO
        .getPersonalDataCompliance()
        .forEach(
            option -> {
              if (option != null) {
                personalDataComplianceList.add(option);
              }
            });
    dmp.setPersonalDataCompliance(personalDataComplianceList);

    List<ESecurityMeasure> sensitiveDataSecurityList = new ArrayList<>();
    dmpDO
        .getSensitiveDataSecurity()
        .forEach(
            option -> {
              if (option != null) {
                sensitiveDataSecurityList.add(option);
              }
            });
    dmp.setSensitiveDataSecurity(sensitiveDataSecurityList);

    List<EAgreement> legalRestrictionsDocumentsList = new ArrayList<>();
    dmpDO
        .getLegalRestrictionsDocuments()
        .forEach(
            option -> {
              if (option != null) {
                legalRestrictionsDocumentsList.add(option);
              }
            });
    dmp.setLegalRestrictionsDocuments(legalRestrictionsDocumentsList);

    // update existing Contributor objects and create new ones
    dmpDO
        .getContributors()
        .forEach(
            contributorDO -> {
              Optional<Contributor> contributorOptional =
                  contributorList.stream()
                      .filter(
                          contributor ->
                              contributorDO.getId() != null
                                  && contributorDO.getId().equals(contributor.id))
                      .findFirst();
              if (contributorOptional.isPresent()) {
                Contributor contributor = contributorOptional.get();
                ContributorDOMapper.mapDOtoEntity(contributorDO, contributor);
              } else {
                Contributor contributor = new Contributor();
                ContributorDOMapper.mapDOtoEntity(contributorDO, contributor);
                contributor.setDmp(dmp);
                contributorList.add(contributor);
              }
            });

    // remove all existing Dataset objects, that are not included in the DO anymore
    List<Dataset> datasetList = dmp.getDatasetList();
    List<Dataset> datasetListToRemove = new ArrayList<>();
    datasetList.forEach(
        dataset -> {
          Optional<DatasetDO> datasetDOOptional =
              dmpDO.getDatasets().stream()
                  .filter(
                      datasetDO ->
                          datasetDO.getId() != null && datasetDO.getId().equals(dataset.id))
                  .findFirst();
          if (datasetDOOptional.isEmpty()) {
            datasetListToRemove.add(dataset);
          }
        });
    datasetList.removeAll(datasetListToRemove);

    // update existing Dataset objects and create new ones
    dmpDO
        .getDatasets()
        .forEach(
            datasetDO -> {
              Optional<Dataset> datasetOptional =
                  datasetList.stream()
                      .filter(
                          dataset ->
                              datasetDO.getId() != null && datasetDO.getId().equals(dataset.id))
                      .findFirst();
              if (datasetOptional.isPresent()) {
                Dataset dataset = datasetOptional.get();
                DatasetDOMapper.mapDOtoEntity(datasetDO, dataset, mapperService);
              } else {
                Dataset dataset = new Dataset();
                DatasetDOMapper.mapDOtoEntity(datasetDO, dataset, mapperService);
                dataset.setDmp(dmp);
                datasetList.add(dataset);
              }
            });

    // set Deletion Person to null if Contributor was removed
    for (Contributor con : contributorListToRemove) {
      datasetList.forEach(
          dataset -> {
            if (dataset.getDeletionPerson() != null
                && dataset.getDeletionPerson().id.equals(con.id)) {
              dataset.setDeletionPerson(null);
            }
          });
    }

    // remove all existing Host objects, that are not included in the DO anymore
    List<Host> hostList = dmp.getHostList();
    List<Host> hostListToRemove = new ArrayList<>();
    hostList.forEach(
        host -> {
          boolean keep = false;

          if (Repository.class.isAssignableFrom(host.getClass())) {
            keep |=
                dmpDO.getRepositories().stream()
                    .anyMatch(hostDO -> hostDO.getId() != null && hostDO.getId().equals(host.id));
          }
          if (Storage.class.isAssignableFrom(host.getClass())) {
            keep |=
                dmpDO.getStorage().stream()
                    .anyMatch(hostDO -> hostDO.getId() != null && hostDO.getId().equals(host.id));
          }

          if (ExternalStorage.class.isAssignableFrom(host.getClass())) {
            keep |=
                dmpDO.getExternalStorage().stream()
                    .anyMatch(hostDO -> hostDO.getId() != null && hostDO.getId().equals(host.id));
          }

          if (!keep) {
            hostListToRemove.add(host);
          }
        });

    hostList.removeAll(hostListToRemove);

    // update existing Repository objects and create new ones
    dmpDO
        .getRepositories()
        .forEach(
            hostDO -> {
              Optional<Host> hostOptional =
                  hostList.stream()
                      .filter(host -> hostDO.getId() != null && hostDO.getId().equals(host.id))
                      .findFirst();
              Repository host;
              if (hostOptional.isPresent()) {
                host = (Repository) hostOptional.get();
                HostDOMapper.mapDOtoEntity(hostDO, host);
                RepositoryDOMapper.mapDOtoEntity(hostDO, host);
              } else {
                host = new Repository();
                HostDOMapper.mapDOtoEntity(hostDO, host);
                RepositoryDOMapper.mapDOtoEntity(hostDO, host);
                host.setDmp(dmp);
                hostList.add(host);
              }
              determineDistributions(dmp, hostDO, host);
            });

    // create Storage objects
    dmpDO
        .getStorage()
        .forEach(
            hostDO -> {
              Optional<Host> hostOptional =
                  hostList.stream()
                      .filter(host -> hostDO.getId() != null && hostDO.getId().equals(host.id))
                      .findFirst();
              Storage host;
              if (hostOptional.isPresent()) {
                host = (Storage) hostOptional.get();
                HostDOMapper.mapDOtoEntity(hostDO, host);
                StorageDOMapper.mapDOtoEntity(hostDO, host, mapperService);
              } else {
                host = new Storage();
                HostDOMapper.mapDOtoEntity(hostDO, host);
                StorageDOMapper.mapDOtoEntity(hostDO, host, mapperService);
                host.setDmp(dmp);
                hostList.add(host);
              }
              determineDistributions(dmp, hostDO, host);
            });

    // create ExternalStorage
    dmpDO
        .getExternalStorage()
        .forEach(
            hostDO -> {
              Optional<Host> hostOptional =
                  hostList.stream()
                      .filter(host -> hostDO.getId() != null && hostDO.getId().equals(host.id))
                      .findFirst();
              ExternalStorage host;
              if (hostOptional.isPresent()) {
                host = (ExternalStorage) hostOptional.get();
                HostDOMapper.mapDOtoEntity(hostDO, host);
                ExternalStorageDOMapper.mapDOtoEntity(hostDO, host);
              } else {
                host = new ExternalStorage();
                HostDOMapper.mapDOtoEntity(hostDO, host);
                ExternalStorageDOMapper.mapDOtoEntity(hostDO, host);
                host.setDmp(dmp);
                hostList.add(host);
              }
              determineDistributions(dmp, hostDO, host);
            });

    // remove all existing Cost objects, that are not included in the DO anymore
    List<Cost> costList = dmp.getCosts();
    List<Cost> costListToRemove = new ArrayList<>();
    costList.forEach(
        cost -> {
          Optional<CostDO> costDOOptional =
              dmpDO.getCosts().stream()
                  .filter(costDO -> costDO.getId() != null && costDO.getId().equals(cost.id))
                  .findFirst();
          if (costDOOptional.isEmpty()) {
            costListToRemove.add(cost);
          }
        });
    costList.removeAll(costListToRemove);

    // update existing Costs objects and create new ones
    dmpDO
        .getCosts()
        .forEach(
            costDO -> {
              Optional<Cost> costOptional =
                  costList.stream()
                      .filter(cost -> costDO.getId() != null && costDO.getId().equals(cost.id))
                      .findFirst();
              if (costOptional.isPresent()) {
                Cost cost = costOptional.get();
                CostDOMapper.mapDOtoEntity(costDO, cost);
              } else {
                Cost cost = new Cost();
                CostDOMapper.mapDOtoEntity(costDO, cost);
                cost.setDmp(dmp);
                costList.add(cost);
              }
            });

    return dmp;
  }

  private void determineDistributions(Dmp dmp, HostDO hostDO, Host host) {
    // convert datasetHash to id references from dataset to hosts
    List<Distribution> distributionList = host.getDistributionList();
    List<Distribution> distributionUpdatedList = new ArrayList<>();

    dmp.getDatasetList()
        .forEach(
            dataset -> {
              if (hostDO.getDatasets().contains(dataset.getReferenceHash())) {

                Distribution distribution = new Distribution();
                distribution.setHost(host);
                distribution.setDataset(dataset);

                Optional<Distribution> distributionOptional =
                    distributionList.stream()
                        .filter(
                            distribution1 ->
                                Objects.equals(distribution1.getDataset().getId(), dataset.getId()))
                        .findFirst();

                if (distributionOptional.isEmpty()) distributionList.add(distribution);
                else distribution = distributionOptional.get();
                distributionUpdatedList.add(distribution);
              }
            });
    List<Distribution> distributionRemoveList = new ArrayList<>();
    distributionList.forEach(
        distribution -> {
          if (!distributionUpdatedList.contains(distribution))
            distributionRemoveList.add(distribution);
        });
    distributionList.removeAll(distributionRemoveList);
  }
}
