package org.damap.base.rest.storage;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import lombok.experimental.UtilityClass;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.domain.InternalStorage;
import org.damap.base.repo.InternalStorageRepo;

@UtilityClass
@JBossLog
public class InternalStorageValidator {

  public void validateForCreation(InternalStorageDO internalStorageDO) {

    try {
      URI.create(internalStorageDO.getUrl());
    } catch (IllegalArgumentException e) {
      throw new ClientErrorException("Invalid URL format", Response.Status.BAD_REQUEST);
    }

    if (internalStorageDO.getTranslations() == null
        || internalStorageDO.getTranslations().isEmpty()) {
      throw new ClientErrorException(
          "Translations list cannot be null or empty, at least one translation needed",
          Response.Status.BAD_REQUEST);
    }
  }

  public void validateForUpdate(
      String id, InternalStorageRepo internalStorageRepo, InternalStorageDO internalStorageDO)
      throws NotFoundException {

    try {
      URI.create(internalStorageDO.getUrl());
    } catch (IllegalArgumentException e) {
      throw new ClientErrorException("Invalid URL format", Response.Status.BAD_REQUEST);
    }

    InternalStorage internalStorage = internalStorageRepo.findById(Long.parseLong(id));

    if (internalStorage == null) {
      throw new NotFoundException("No internal storage with ID " + id + " found");
    }
  }

  public boolean storageIdExists(Long storageId) {
    return InternalStorage.findById(storageId) != null;
  }

  public MultivaluedMap<String, String> validateSearchParameters(
      MultivaluedMap<String, String> queryParams) {
    List<String> allowedParams =
        Arrays.asList("id", "url", "active", "storageLocation", "backupLocation");

    MultivaluedMap<String, String> queryParamsChecked = new MultivaluedHashMap<>();

    queryParamsChecked.putAll(queryParams);
    queryParamsChecked.keySet().removeIf(key -> !allowedParams.contains(key));

    queryParamsChecked.values().removeIf(List::isEmpty);

    return queryParamsChecked;
  }
}
