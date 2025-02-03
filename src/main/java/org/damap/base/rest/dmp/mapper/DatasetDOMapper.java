package org.damap.base.rest.dmp.mapper;

import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.damap.base.domain.Dataset;
import org.damap.base.domain.Identifier;
import org.damap.base.domain.TechnicalResource;
import org.damap.base.enums.EDataAccessType;
import org.damap.base.enums.EDataType;
import org.damap.base.rest.dmp.domain.ContributorDO;
import org.damap.base.rest.dmp.domain.DatasetDO;
import org.damap.base.rest.dmp.domain.IdentifierDO;
import org.damap.base.rest.dmp.domain.TechnicalResourceDO;

/** DatasetDOMapper class. */
@UtilityClass
public class DatasetDOMapper {

  /**
   * mapEntityToDO.
   *
   * @param dataset a {@link org.damap.base.domain.Dataset} object
   * @param datasetDO a {@link org.damap.base.rest.dmp.domain.DatasetDO} object
   * @return a {@link org.damap.base.rest.dmp.domain.DatasetDO} object
   */
  public DatasetDO mapEntityToDO(Dataset dataset, DatasetDO datasetDO) {
    datasetDO.setId(dataset.id);
    datasetDO.setTitle(dataset.getTitle());
    datasetDO.setSize(dataset.getSize());
    datasetDO.setDescription(dataset.getDescription());
    datasetDO.setPersonalData(dataset.getPersonalData());
    datasetDO.setSensitiveData(dataset.getSensitiveData());
    datasetDO.setLegalRestrictions(dataset.getLegalRestrictions());
    datasetDO.setLicense(dataset.getLicense());
    datasetDO.setStartDate(dataset.getStart());
    datasetDO.setReferenceHash(dataset.getReferenceHash());
    datasetDO.setDelete(dataset.getDelete());
    datasetDO.setDateOfDeletion(dataset.getDateOfDeletion());
    datasetDO.setReasonForDeletion(dataset.getReasonForDeletion());
    datasetDO.setRetentionPeriod(dataset.getRetentionPeriod());
    datasetDO.setSource(dataset.getSource());
    if (dataset.getDataAccess() != null) datasetDO.setDataAccess(dataset.getDataAccess());
    if (dataset.getSelectedProjectMembersAccess() != null)
      datasetDO.setSelectedProjectMembersAccess(dataset.getSelectedProjectMembersAccess());
    if (dataset.getOtherProjectMembersAccess() != null)
      datasetDO.setOtherProjectMembersAccess(dataset.getOtherProjectMembersAccess());
    if (dataset.getPublicAccess() != null) datasetDO.setPublicAccess(dataset.getPublicAccess());
    if (dataset.getDeletionPerson() != null)
      datasetDO.setDeletionPerson(
          ContributorDOMapper.mapEntityToDO(dataset.getDeletionPerson(), new ContributorDO()));
    if (dataset.getDatasetIdentifier() != null)
      datasetDO.setDatasetId(
          IdentifierDOMapper.mapEntityToDO(dataset.getDatasetIdentifier(), new IdentifierDO()));

    List<EDataType> typeList = new ArrayList<>();
    dataset
        .getType()
        .forEach(
            option -> {
              if (option != null) {
                typeList.add(option);
              }
            });
    datasetDO.setType(typeList);
    datasetDO.setFileFormat(dataset.getFileFormat());

    List<TechnicalResourceDO> technicalResourceDOS = new ArrayList<>();
    for (TechnicalResource technicalResource : dataset.getTechnicalResources()) {
      TechnicalResourceDO technicalResourceDO = new TechnicalResourceDO();
      technicalResourceDO.setName(technicalResource.getName());
      technicalResourceDO.setDescription(technicalResource.getDescription());
      technicalResourceDOS.add(technicalResourceDO);
    }

    datasetDO.setTechnicalResources(technicalResourceDOS);

    return datasetDO;
  }

  /**
   * mapDOtoEntity.
   *
   * @param datasetDO a {@link org.damap.base.rest.dmp.domain.DatasetDO} object
   * @param dataset a {@link org.damap.base.domain.Dataset} object
   * @param mapperService a {@link org.damap.base.rest.dmp.mapper.MapperService} object
   * @return a {@link org.damap.base.domain.Dataset} object
   */
  public Dataset mapDOtoEntity(DatasetDO datasetDO, Dataset dataset, MapperService mapperService) {
    if (datasetDO.getId() != null) dataset.id = datasetDO.getId();
    dataset.setTitle(datasetDO.getTitle());
    dataset.setSize(datasetDO.getSize());
    dataset.setDescription(datasetDO.getDescription());
    dataset.setPersonalData(datasetDO.getPersonalData());
    dataset.setSensitiveData(datasetDO.getSensitiveData());
    dataset.setLegalRestrictions(datasetDO.getLegalRestrictions());
    dataset.setLicense(datasetDO.getLicense());
    if (datasetDO.getDataAccess() == EDataAccessType.CLOSED) {
      dataset.setStart(null);
    } else {
      dataset.setStart(datasetDO.getStartDate());
    }
    dataset.setReferenceHash(datasetDO.getReferenceHash());
    dataset.setDataAccess(datasetDO.getDataAccess());
    dataset.setSelectedProjectMembersAccess(datasetDO.getSelectedProjectMembersAccess());
    dataset.setOtherProjectMembersAccess(datasetDO.getOtherProjectMembersAccess());
    dataset.setPublicAccess(datasetDO.getPublicAccess());
    dataset.setDelete(datasetDO.getDelete());
    dataset.setDateOfDeletion(datasetDO.getDateOfDeletion());
    dataset.setReasonForDeletion(datasetDO.getReasonForDeletion());
    dataset.setRetentionPeriod(datasetDO.getRetentionPeriod());
    dataset.setSource(datasetDO.getSource());
    if (datasetDO.getDeletionPerson() != null && datasetDO.getDeletionPerson().getId() != null)
      dataset.setDeletionPerson(
          mapperService.getDeletionPerson(datasetDO.getDeletionPerson().getId()));
    else dataset.setDeletionPerson(null);

    if (datasetDO.getDatasetId() != null)
      dataset.setDatasetIdentifier(
          IdentifierDOMapper.mapDOtoEntity(datasetDO.getDatasetId(), new Identifier()));

    List<EDataType> typeList = new ArrayList<>();
    datasetDO
        .getType()
        .forEach(
            option -> {
              if (option != null) {
                typeList.add(option);
              }
            });
    dataset.setType(typeList);
    dataset.setFileFormat(datasetDO.getFileFormat());

    List<TechnicalResource> existingResources = dataset.getTechnicalResources();
    existingResources.clear();

    if (datasetDO.getTechnicalResources() != null) {
      for (TechnicalResourceDO technicalResourceDO : datasetDO.getTechnicalResources()) {
        TechnicalResource technicalResource = new TechnicalResource();
        technicalResource.setName(technicalResourceDO.getName());
        technicalResource.setDescription(technicalResourceDO.getDescription());
        technicalResource.setDataset(dataset);
        existingResources.add(technicalResource);
      }
    }

    return dataset;
  }
}
