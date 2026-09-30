package org.damap.base.rest.dmp.service;

import jakarta.validation.ValidationException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.domain.*;
import org.damap.base.enums.*;
import org.damap.base.rest.dmp.domain.DatasetDO;
import org.damap.base.rest.dmp.domain.DmpDO;
import org.damap.base.rest.dmp.domain.HostDO;
import org.damap.base.rest.dmp.domain.StorageDO;
import org.damap.base.rest.storage.InternalStorageService;

/** DmpConsistencyUtility class. */
@UtilityClass
@JBossLog
public class DmpConsistencyUtility {

  private static final String STORAGE_NOT_ACTIVE = "Storage is not active";

  /**
   * Adapts a given DMP so that it's information is consistent.
   *
   * @param dmpDO the DMP to check and adapt.
   */
  public void enforceDmpConsistency(DmpDO dmpDO) {

    // Remove datasets by source if not specified
    if (dmpDO.getDataKind() != EDataKind.SPECIFY) {
      removeDatasetsBySource(dmpDO, EDataSource.NEW);
    }
    if (dmpDO.getReusedDataKind() != EDataKind.SPECIFY) {
      removeDatasetsBySource(dmpDO, EDataSource.REUSED);
    }

    // Reset form if no datasets are specified
    if (dmpDO.getDataKind() != EDataKind.SPECIFY
        && dmpDO.getReusedDataKind() != EDataKind.SPECIFY) {
      // Remove all dataset related info from dmp
      removeDatasetRelatedInfo(dmpDO);
    }
    if (!(dmpDO.getDataKind() == EDataKind.NONE && dmpDO.getReusedDataKind() == EDataKind.NONE)) {
      dmpDO.setNoDataExplanation(null);
    }

    // Set conditional info
    setConditionalDmpInfo(dmpDO);
    enforceDatasetConsistency(dmpDO);
  }

  /**
   * Validates that all internal storage used in the new DMP are active. If there is no old DMP, it
   * retrieves the storage IDs from the new DMP and checks their active status using the {@link
   * InternalStorageService}. If comparing with an old DMP, the method checks any new storage IDs
   * added in the new DMP and ensures they are active. If any storage is inactive, a {@link
   * ValidationException} is thrown. A storage that is already in the old DMP is not checked for
   * active status.
   *
   * @param newDmp the new DMP
   * @param oldDmp the old DMP
   * @param internalStorageService the internal storage service
   * @throws ValidationException if at least one storage is not active
   */
  public void enforceActiveStorage(
      DmpDO newDmp, Dmp oldDmp, InternalStorageService internalStorageService)
      throws ValidationException {

    // New DMP used storage IDs
    List<Long> newDmpStorageIds = new ArrayList<>();
    for (StorageDO storageDO : newDmp.getStorage()) {
      newDmpStorageIds.add(storageDO.getInternalStorageId());
    }

    // Old DMP used storage IDs or empty list if no old DMP
    List<Long> oldDmpStorageIds = new ArrayList<>();
    if (oldDmp != null) {
      for (Host host : oldDmp.getHostList()) {
        if (host instanceof Storage storage) {
          InternalStorage internalStorage = storage.getInternalStorageId();
          if (internalStorage != null) {
            oldDmpStorageIds.add(internalStorage.id);
          }
        }
      }
    }

    // Difference between new and old DMP storage IDs
    newDmpStorageIds.removeAll(oldDmpStorageIds);

    newDmpStorageIds.removeIf(Objects::isNull);

    for (Long id : newDmpStorageIds) {
      InternalStorage storage = InternalStorage.findById(id);
      if (storage.isActive()) {
        continue;
      }
      throw new ValidationException(STORAGE_NOT_ACTIVE);
    }
  }

  /**
   * Removes all datasets from {@code dmpDO} that are of type {@code source}.
   *
   * @param dmpDO the DMP to be edited.
   * @param source the source type of the datasets to be removed.
   */
  private void removeDatasetsBySource(DmpDO dmpDO, EDataSource source) {
    List<DatasetDO> datasets = dmpDO.getDatasets();
    for (DatasetDO datasetDO : datasets) {
      if (datasetDO.getSource() == source) {
        removeDatasetFromHost(datasetDO, dmpDO.getStorage());
        removeDatasetFromHost(datasetDO, dmpDO.getExternalStorage());
        removeDatasetFromHost(datasetDO, dmpDO.getRepositories());
      }
    }
    dmpDO.setDatasets(
        datasets.stream()
            .filter(dataset -> dataset.getSource() != source)
            .collect(Collectors.toList()));
  }

  /**
   * Removes all datasets and dataset related information from {@code dmpDO}. Used when no data is
   * specified.
   *
   * @param dmpDO the DMP to be edited.
   */
  private void removeDatasetRelatedInfo(DmpDO dmpDO) {
    dmpDO.setDatasets(new ArrayList<>());
    dmpDO.setDataGeneration(null);
    dmpDO.setDataRightsAndAccessControl(null);

    resetDocumentation(dmpDO);
    resetReuse(dmpDO);

    dmpDO.setRepositories(new ArrayList<>());
    dmpDO.setStorage(new ArrayList<>());
    dmpDO.setExternalStorage(new ArrayList<>());

    dmpDO.setCostsExist(false);
  }

  /**
   * Resets the all documentation related information of {@code dmpDO}. Used when no data is
   * specified.
   *
   * @param dmpDO the DMP to be edited.
   */
  private void resetDocumentation(DmpDO dmpDO) {
    dmpDO.setMetadata(null);
    dmpDO.setStructure(null);
    dmpDO.setDocumentation(null);
    dmpDO.setDataQuality(new ArrayList<>());
  }

  /**
   * Resets the all reuse information of {@code dmpDO}. Used when no data is specified.
   *
   * @param dmpDO the DMP to be edited.
   */
  private void resetReuse(DmpDO dmpDO) {
    dmpDO.setTargetAudience(null);
    dmpDO.setTools(null);
  }

  private void removeDatasetFromHost(DatasetDO datasetDO, List<? extends HostDO> hosts) {
    for (HostDO host : hosts) {
      if (host.getDatasets() != null) {
        host.setDatasets(
            host.getDatasets().stream()
                .filter(hash -> !hash.equals(datasetDO.getReferenceHash()))
                .collect(Collectors.toList()));
      }
    }
  }

  private void enforceDatasetConsistency(DmpDO dmpDO) {
    if (!hasDatasetsOfAccessType(dmpDO.getDatasets(), EDataAccessType.RESTRICTED)) {
      dmpDO.setRestrictedAccessInfo(null);
      dmpDO.setRestrictedDataAccess(null);
    }
    if (!hasDatasetsOfAccessType(dmpDO.getDatasets(), EDataAccessType.CLOSED)) {
      dmpDO.setClosedAccessInfo(null);
    }
    // For personal data etc. set dataset property
    if (!Boolean.TRUE.equals(dmpDO.getSensitiveData()))
      dmpDO.getDatasets().forEach(dataset -> dataset.setSensitiveData(false));
    if (!Boolean.TRUE.equals(dmpDO.getPersonalData()))
      dmpDO.getDatasets().forEach(dataset -> dataset.setPersonalData(false));
    if (!Boolean.TRUE.equals(dmpDO.getLegalRestrictions()))
      dmpDO.getDatasets().forEach(dataset -> dataset.setLegalRestrictions(false));

    for (DatasetDO datasetDO : dmpDO.getDatasets()) {
      // Deletion
      // Sets deletion to false in case it was set to true and then the dataset was set to OPEN
      if (datasetDO.getDataAccess() != EDataAccessType.CLOSED) {
        datasetDO.setDelete(false);
      }
      if (!Boolean.TRUE.equals(datasetDO.getDelete())) {
        datasetDO.setDeletionPerson(null);
        datasetDO.setReasonForDeletion(null);
        datasetDO.setDateOfDeletion(null);
      }
    }

    HashSet<String> seenDatasetDOI = new HashSet<>();
    List<DatasetDO> uniqueDatasets = new ArrayList<>();

    for (DatasetDO datasetDO : dmpDO.getDatasets()) {
      if (datasetDO.getSource() == EDataSource.REUSED) {
        // We have a reused dataset
        if (datasetDO.getDatasetId() != null
            && datasetDO.getDatasetId().getType() == EIdentifierType.DOI) {
          // No duplicate DOIs - only add if not seen before
          String datasetDOI = datasetDO.getDatasetId().getIdentifier();
          if (!seenDatasetDOI.contains(datasetDOI)) {
            uniqueDatasets.add(datasetDO);
            seenDatasetDOI.add(datasetDOI);
          }
        } else {
          // Manual entry - always add
          uniqueDatasets.add(datasetDO);
        }
      } else {
        // New dataset - always add
        uniqueDatasets.add(datasetDO);
      }
    }

    dmpDO.setDatasets(uniqueDatasets);
  }

  /**
   * Sets all conditional properties of a given DMP.
   *
   * @param dmpDO the DMP to be edited.
   */
  private void setConditionalDmpInfo(DmpDO dmpDO) {
    unsetOtherIfNotSpecified(
        dmpDO.getDataQuality(), EDataQualityType.OTHERS, dmpDO::setOtherDataQuality);

    // Unset legal info if no datasets exist
    boolean hasDatasets = dmpDO.getDatasets() != null && !dmpDO.getDatasets().isEmpty();

    // Sensitive Data
    unsetListIfFalseOrNull(dmpDO.getSensitiveData(), hasDatasets, dmpDO::setSensitiveDataSecurity);
    unsetOtherIfNotSpecified(
        dmpDO.getSensitiveDataSecurity(),
        ESecurityMeasure.OTHER,
        dmpDO::setOtherDataSecurityMeasures);
    if (!Boolean.TRUE.equals(dmpDO.getSensitiveData()) || !hasDatasets) {
      dmpDO.setSensitiveDataAccess(null);
    }

    // Personal Data
    unsetListIfFalseOrNull(dmpDO.getPersonalData(), hasDatasets, dmpDO::setPersonalDataCompliance);
    unsetOtherIfNotSpecified(
        dmpDO.getPersonalDataCompliance(),
        EComplianceType.OTHER,
        dmpDO::setOtherPersonalDataCompliance);

    // Legal Restrictions
    unsetListIfFalseOrNull(
        dmpDO.getLegalRestrictions(), hasDatasets, dmpDO::setLegalRestrictionsDocuments);
    unsetOtherIfNotSpecified(
        dmpDO.getLegalRestrictionsDocuments(),
        EAgreement.OTHER,
        dmpDO::setOtherLegalRestrictionsDocument);
    if (!Boolean.TRUE.equals(dmpDO.getLegalRestrictions()) || !hasDatasets) {
      dmpDO.setLegalRestrictionsComment(null);
    }

    if (dmpDO.getExternalStorage().isEmpty()) {
      dmpDO.setExternalStorageInfo(null);
    }

    // Costs
    unsetListIfFalseOrNull(dmpDO.getCostsExist(), hasDatasets, dmpDO::setCosts);
  }

  private boolean hasDatasetsOfAccessType(
      List<DatasetDO> datasetDOList, EDataAccessType accessType) {
    return datasetDOList.stream().anyMatch(datasetDO -> datasetDO.getDataAccess() == accessType);
  }

  private <R> void unsetListIfFalseOrNull(
      Boolean condition1, Boolean condition2, Consumer<List<R>> consumer) {
    if (!(Boolean.TRUE.equals(condition1) && Boolean.TRUE.equals(condition2))) {
      consumer.accept(new ArrayList<>());
    }
  }

  /**
   * Sets property ({@code consumer}) to {@code null} if {@code enumList} does not contain {@code
   * other}.
   *
   * <p>Used to conditionally set {@code otherSensitiveDataSecurityMeasures}, {@code
   * otherPersonalDataCompliance} and {@code otherLegalRestrictionsDocument}.
   *
   * @param enumList the enum list to be checked for {@code other} option.
   * @param other the enum value to check the list for.
   * @param consumer the property to be set to null if {@code other} is not specified.
   * @param <R> the enum type.
   */
  private <R extends Enum<R>> void unsetOtherIfNotSpecified(
      List<R> enumList, R other, Consumer<String> consumer) {
    if (enumList == null || !enumList.contains(other)) {
      consumer.accept(null);
    }
  }
}
