package org.damap.base.rest.storage;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.domain.InternalStorage;
import org.damap.base.domain.InternalStorageTranslation;
import org.damap.base.domain.Storage;
import org.damap.base.repo.InternalStorageRepo;
import org.damap.base.repo.InternalStorageTranslationRepo;
import org.damap.base.repo.StorageRepo;
import org.damap.base.rest.base.ResultList;
import org.damap.base.rest.base.Search;
import org.damap.base.rest.base.service.ServiceCreate;
import org.damap.base.rest.base.service.ServiceDelete;
import org.damap.base.rest.base.service.ServiceRead;
import org.damap.base.rest.base.service.ServiceSearch;
import org.damap.base.rest.base.service.ServiceUpdate;

/** InternalStorageService class. */
@ApplicationScoped
@JBossLog
public class InternalStorageService
    implements ServiceCreate<InternalStorageDO, InternalStorageDO>,
        ServiceDelete,
        ServiceRead<InternalStorageDO>,
        ServiceSearch<InternalStorageDO>,
        ServiceUpdate<InternalStorageDO, InternalStorageDO> {

  @Inject InternalStorageTranslationRepo internalStorageTranslationRepo;

  @Inject InternalStorageRepo internalStorageRepo;

  @Inject StorageRepo storageRepo;

  /**
   * create a new internal storage option.
   *
   * @param data a {@link org.damap.base.rest.storage.InternalStorageDO} object
   * @return a {@link org.damap.base.rest.storage.InternalStorageDO} object
   */
  @Override
  @Transactional
  public InternalStorageDO create(InternalStorageDO data) {
    InternalStorageValidator.validateForCreation(data);

    InternalStorage internalStorage =
        InternalStorageDOMapper.mapDOToEntity(data, new InternalStorage());
    internalStorage.persist();

    for (InternalStorageTranslationDO translationDO : data.getTranslations()) {
      translationDO.setStorageId(internalStorage.id);
      InternalStorageTranslationValidator.validateForCreation(
          translationDO, internalStorageRepo, internalStorageTranslationRepo);
      InternalStorageTranslation translation =
          InternalStorageTranslationDOMapper.mapDOToTranslationEntityForCreation(
              translationDO, internalStorage);
      translation.setInternalStorageId(internalStorage);
      translation.persist();
    }

    return read(String.valueOf(internalStorage.id));
  }

  /**
   * read a specific internal storage option.
   *
   * @param id a {@link java.lang.String} object
   * @param queryParams a {@link jakarta.ws.rs.core.MultivaluedMap} object
   * @return a {@link org.damap.base.rest.storage.InternalStorageDO} object
   */
  @Override
  public InternalStorageDO read(String id, MultivaluedMap<String, String> queryParams) {

    InternalStorage internalStorage =
        internalStorageRepo
            .findByIdOptional(Long.parseLong(id))
            .orElseThrow(
                () -> new NotFoundException("No internal storage with ID " + id + " found"));

    List<InternalStorageTranslation> translations =
        internalStorageTranslationRepo.getAllInternalStorageTranslationsByStorageId(
            Long.parseLong(id));

    return InternalStorageDOMapper.mapEntityToDO(
        internalStorage, new InternalStorageDO(), translations);
  }

  /**
   * update a specific internal storage option.
   *
   * @param id a {@link java.lang.String} object
   * @param data a {@link org.damap.base.rest.storage.InternalStorageDO} object
   * @return a {@link org.damap.base.rest.storage.InternalStorageDO} object, updated
   *     <p>Note: The internalStorageTranslation has to be updated through a separate endpoint
   */
  @Override
  @Transactional
  public InternalStorageDO update(String id, InternalStorageDO data) {
    InternalStorageValidator.validateForUpdate(id, internalStorageRepo, data);

    InternalStorage internalStorage = internalStorageRepo.findById(Long.parseLong(id));
    InternalStorageDOMapper.mapDOToEntity(data, internalStorage);
    internalStorage.persistAndFlush();

    return read(String.valueOf(internalStorage.id));
  }

  /**
   * delete a specific internal storage option.
   *
   * @param id a {@link java.lang.String} object
   */
  @Override
  @Transactional
  public void delete(String id) {
    InternalStorage internalStorage =
        internalStorageRepo
            .findByIdOptional(Long.parseLong(id))
            .orElseThrow(
                () -> new NotFoundException("No internal storage with ID " + id + " found"));

    List<Storage> storages = storageRepo.findByInternalStorageId(internalStorage);

    if (!storages.isEmpty()) {
      throw new ClientErrorException(
          "Internal storage with ID " + id + " is still in use by " + storages.size() + " storages",
          Response.Status.CONFLICT);
    }

    internalStorageTranslationRepo.deleteAllTranslationsForInternalStorage(internalStorage.id);

    internalStorageRepo.delete(internalStorage);
  }

  /**
   * search for internal storage options.
   *
   * @param queryParams a {@link jakarta.ws.rs.core.MultivaluedMap} object
   * @return a {@link org.damap.base.rest.base.ResultList} object, list of all matching internal
   *     storage options
   */
  @Override
  public ResultList<InternalStorageDO> search(MultivaluedMap<String, String> queryParams) {
    queryParams = InternalStorageValidator.validateSearchParameters(queryParams);

    MultivaluedMap<String, Class> fields = getEntityFields();

    MultivaluedMap<String, Object> searchParams = new MultivaluedHashMap<>();
    for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
      String key = entry.getKey();
      List<String> values = entry.getValue();
      if (fields.containsKey(key)) {
        searchParams.addAll(
            key, values.stream().map(v -> convertValue(fields.getFirst(key), v)).toList());
      }
    }

    List<InternalStorage> internalStorageList =
        internalStorageRepo.searchByParameters(searchParams);

    List<InternalStorageDO> internalStorageDOList = new ArrayList<>();

    for (InternalStorage internalStorage : internalStorageList) {
      List<InternalStorageTranslation> translations =
          internalStorageTranslationRepo.getAllInternalStorageTranslationsByStorageId(
              internalStorage.id);
      internalStorageDOList.add(
          InternalStorageDOMapper.mapEntityToDO(
              internalStorage, new InternalStorageDO(), translations));
    }

    Search search = Search.fromMap(queryParams);

    return ResultList.fromItemsAndSearch(internalStorageDOList, search);
  }

  @Override
  public MultivaluedMap<String, Class> getEntityFields() {
    MultivaluedMap<String, Class> fields = new MultivaluedHashMap<>();
    fields.add("id", Long.class);
    fields.add("version", Long.class);
    fields.add("url", String.class);
    fields.add("storageLocation", String.class);
    fields.add("backupLocation", String.class);
    fields.add("active", Boolean.class);
    return fields;
  }
}
