package org.damap.base.rda.dmpcommonstandard;

import io.quarkus.arc.Arc;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.domain.InternalStorage;
import org.damap.base.domain.InternalStorageTranslation;
import org.damap.base.enums.EErrorCode;
import org.damap.base.exception.DamapApiException;
import org.damap.base.r3data.RepositoriesService;
import org.damap.base.r3data.dto.RepositoryDetails;
import org.damap.base.rest.dmp.domain.DatasetDO;
import org.damap.base.rest.dmp.domain.DmpDO;
import org.damap.base.rest.dmp.domain.ExternalStorageDO;
import org.damap.base.rest.dmp.domain.RepositoryDO;
import org.damap.base.rest.dmp.domain.StorageDO;
import org.re3data.schema._2_2.Re3Data;

@JBossLog
/**
 * This class implements Host conversion from and to the RDA DMP Common Standard.
 *
 * <p>Note: While DAMAP uses inheritance for hosts (Repositories, External Storages, and Internal
 * Storages inheriting from a shared Host base), the RDA Common Standard utilizes a flat Host class
 * inside Distribution objects.
 */
public class HostsMapper extends AbstractMapper {
  /** Initialize the mapper with the default settings (strict mode turned on). */
  public HostsMapper() {
    this(true);
  }

  /**
   * Initialize the mapper with an option to turn off strict mode. Only use non-strict mode when a
   * human has to review the DMP afterward, never use it for automated imports!
   *
   * @param strict When enabled, any dropped data will cause a {@link
   *     CommonStandardCompatibilityException}. When disabled, DAMAP will do its best to represent
   *     any imported data in the common format but drop data it cannot represent.
   */
  public HostsMapper(boolean strict) {
    super(strict);
  }

  /**
   * DAMAP to RDA (Export).
   *
   * <p>Manages the mapping of DAMAP dataset hosts to the dataset's distributions. Searches across
   * repositories, external storages, and internal storages, applying fallback logic if no explicit
   * links are present.
   *
   * @param dmp the DAMAP DMP object containing host lists
   * @param datasetDO the DAMAP dataset being mapped
   * @param baseDist the base RDA standard distribution object containing basic metadata
   * @return a list of mapped RDA Distribution objects, each containing an assigned Host
   */
  public List<Distribution> mapDistributions(
      DmpDO dmp, DatasetDO datasetDO, Distribution baseDist) {

    List<Distribution> result = new ArrayList<>();
    if (baseDist == null || datasetDO == null) {
      return result;
    }

    String refHash = datasetDO.getReferenceHash();
    String idStr = String.valueOf(datasetDO.getId());

    // 1. Search and process Repository links
    searchRepositories(dmp, refHash, idStr, baseDist, result);

    // 2. Search and process External Storage links
    searchExternalStorages(dmp, refHash, idStr, baseDist, result);

    // 3. Search and process Internal Storage links
    searchInternalStorages(dmp, refHash, idStr, baseDist, result);

    // 4. Fallback if empty and only 1 dataset + 1 host
    if (result.isEmpty() && dmp.getDatasets() != null && dmp.getDatasets().size() == 1) {
      applySingleHostFallback(dmp, result, baseDist);
    }

    if (result.isEmpty()) {
      result.add(baseDist);
    }

    return result;
  }

  /**
   * Searches for repository hosts linked to the dataset, maps them, and adds them as Distribution
   * objects to the result list (retaining the availableUntil date).
   */
  private void searchRepositories(
      DmpDO dmp, String refHash, String idStr, Distribution baseDist, List<Distribution> result) {
    dmp.getRepositories().stream()
        .filter(repo -> isLinked(repo.getDatasets(), refHash, idStr))
        .forEach(
            repo -> {
              Host mappedHost = mapRepository(repo);
              if (mappedHost != null) {
                Distribution dist = result.isEmpty() ? baseDist : cloneDistribution(baseDist);
                dist.setHost(mappedHost);
                result.add(dist);
              }
            });
  }

  /**
   * Searches for external storage hosts linked to the dataset, maps them, clears the availableUntil
   * date (since it is not a repository), and adds them as Distribution objects.
   */
  private void searchExternalStorages(
      DmpDO dmp, String refHash, String idStr, Distribution baseDist, List<Distribution> result) {
    if (dmp.getExternalStorage() != null) {
      dmp.getExternalStorage().stream()
          .filter(ext -> isLinked(ext.getDatasets(), refHash, idStr))
          .forEach(
              ext -> {
                Distribution dist = result.isEmpty() ? baseDist : cloneDistribution(baseDist);
                dist.setHost(mapExternalStorage(ext));
                dist.setAvailableUntil(null);
                result.add(dist);
              });
    }
  }

  /**
   * Searches for internal storage hosts linked to the dataset, maps them, clears the availableUntil
   * date (since it is not a repository), and adds them as Distribution objects.
   */
  private void searchInternalStorages(
      DmpDO dmp, String refHash, String idStr, Distribution baseDist, List<Distribution> result) {
    if (dmp.getStorage() != null) {
      dmp.getStorage().stream()
          .filter(store -> isLinked(store.getDatasets(), refHash, idStr))
          .forEach(
              store -> {
                Distribution dist = result.isEmpty() ? baseDist : cloneDistribution(baseDist);
                dist.setHost(mapInternalStorage(store));
                dist.setAvailableUntil(null);
                result.add(dist);
              });
    }
  }

  /**
   * Fallback mapping mechanism designed for edge cases where datasets do not have explicit database
   * links to any hosts (repositories, internal storage, or external storage).
   *
   * <p>If the DMP contains exactly one dataset and exactly one host across all categories, this
   * method assumes an implicit 1-to-1 relationship and maps that single host to the dataset.
   *
   * <ul>
   *   <li>Repositories retain the 'availableUntil' retention date from the base distribution.
   *   <li>Internal and external storages have their 'availableUntil' date cleared to null, as there
   *       is no support for it in the DAMAP model.
   * </ul>
   */
  private void applySingleHostFallback(
      DmpDO dmp, List<Distribution> result, Distribution baseDist) {
    if (dmp.getRepositories().size() == 1) {
      Host mappedHost = mapRepository(dmp.getRepositories().get(0));
      if (mappedHost != null) {
        baseDist.setHost(mappedHost);
        result.add(baseDist);
      }
    } else if (dmp.getExternalStorage() != null && dmp.getExternalStorage().size() == 1) {
      baseDist.setHost(mapExternalStorage(dmp.getExternalStorage().get(0)));
      baseDist.setAvailableUntil(null);
      result.add(baseDist);
    } else if (dmp.getStorage() != null && dmp.getStorage().size() == 1) {
      baseDist.setHost(mapInternalStorage(dmp.getStorage().get(0)));
      baseDist.setAvailableUntil(null);
      result.add(baseDist);
    }
  }

  /**
   * Helper method to determine if a storage/repository is linked to a specific dataset. Maps
   * dynamically on both transient reference hashes and persisted database IDs.
   *
   * @param datasets the list of dataset references on the host
   * @param refHash the transient reference hash of the dataset
   * @param idStr the database ID string of the dataset
   * @return true if the host is linked to the dataset, false otherwise
   */
  private boolean isLinked(List<String> datasets, String refHash, String idStr) {
    if (datasets == null) return false;
    return (idStr != null && datasets.contains(idStr))
        || (refHash != null && datasets.contains(refHash));
  }

  /**
   * DAMAP to RDA (Export).
   *
   * <p>Converts a DAMAP Repository domain object to an RDA standard Host. Attempts to fetch the
   * official URL and Title from the re3data service when possible.
   *
   * @param repo the DAMAP repository object
   * @return the mapped RDA standard Host
   */
  private Host mapRepository(RepositoryDO repo) {
    String repoUrl = null;
    String repoTitle = repo.getTitle();
    String r3dataId = repo.getRepositoryId();
    if (r3dataId != null && !r3dataId.isBlank()) {
      try {
        RepositoriesService repositoriesService =
            Arc.container().instance(RepositoriesService.class).get();
        Re3Data re3Data = repositoriesService.getById(repo.getRepositoryId());
        if (re3Data != null && !re3Data.getRepository().isEmpty()) {
          var firstRepo = re3Data.getRepository().get(0);
          repoUrl = firstRepo.getRepositoryURL();
          if (firstRepo.getRepositoryName() != null
              && firstRepo.getRepositoryName().getValue() != null
              && !firstRepo.getRepositoryName().getValue().isBlank()) {
            repoTitle = firstRepo.getRepositoryName().getValue();
          }
        }
      } catch (DamapApiException e) {
        EErrorCode errorCode = e.getPayload().errorCode();
        if (errorCode == EErrorCode.RE3DATA_NOT_FOUND) {
          log.warnv("Repository registry ID {0} not found in re3data", r3dataId);
        } else if (errorCode == EErrorCode.RE3DATA_NOT_AVAILABLE) {
          log.errorv(
              "re3data service not available when fetching metadata for ID {0}", r3dataId, e);
        } else {
          log.errorv("Unexpected API error retrieving repository metadata for ID {0}", r3dataId, e);
        }
        return null;
      } catch (Exception e) {
        log.errorv("Unexpected exception retrieving repository name for ID {0}", r3dataId, e);
        return null;
      }
    }

    if (repoTitle == null || repoTitle.isBlank()) {
      repoTitle = "Repository";
    }

    Host host = new Host().title(repoTitle).url(repoUrl);
    if (r3dataId != null && !r3dataId.isBlank()) {
      HostID hostId = new HostID();
      hostId.setIdentifier(r3dataId);
      hostId.setType("re3data");

      List<HostID> hostIdList = new ArrayList<>();
      hostIdList.add(hostId);
      host.setHostId(hostIdList);
    }
    return host;
  }

  /**
   * DAMAP to RDA (Export).
   *
   * <p>Converts a DAMAP External Storage domain object into an RDA standard Host.
   *
   * @param ext the DAMAP external storage object
   * @return the mapped RDA standard Host
   */
  private Host mapExternalStorage(ExternalStorageDO ext) {
    return new Host()
        .title(ext.getTitle() != null ? ext.getTitle() : "External Storage")
        // TODO: Map this to the real storage URL in the future
        .url(ext.getUrl())
        // TODO: Export backup location (requires controlled ISO 3166-1 vocabulary instead of free
        // text)
        .backupFrequency(ext.getBackupFrequency());
  }

  /**
   * DAMAP to RDA (Export).
   *
   * <p>Converts a DAMAP Internal Storage domain object into an RDA standard Host. Attempts to load
   * specific backing Storage metadata (URL, Backup Location) if configured.
   *
   * @param store the DAMAP internal storage object
   * @return the mapped RDA standard Host
   */
  private Host mapInternalStorage(StorageDO store) {
    String url = null;

    if (store.getInternalStorageId() != null) {
      try {
        InternalStorage internalStore = InternalStorage.findById(store.getInternalStorageId());
        if (internalStore != null) {
          url = internalStore.getUrl();
        }
      } catch (Exception e) {
        log.errorv(
            "Failed to retrieve internal storage for ID {0}, error: {1}",
            store.getInternalStorageId(), e.getMessage());
      }
    }

    // TODO: Export backup location (requires controlled ISO 3166-1 vocabulary instead of free text)
    return new Host().title(store.getTitle() != null ? store.getTitle() : "Storage").url(url);
  }

  /**
   * Clones a distribution object. Note: This helper method is maintained here because the {@link
   * Distribution} class is generated from the RDA Common Standard schema, preventing the addition
   * of a clone constructor.
   */
  private Distribution cloneDistribution(Distribution source) {
    Distribution target = new Distribution();
    target.setTitle(source.getTitle());
    target.setDataAccess(source.getDataAccess());
    target.setAvailableUntil(source.getAvailableUntil());
    target.setByteSize(source.getByteSize());
    target.setFormat(source.getFormat());
    target.setLicense(source.getLicense());
    return target;
  }

  /**
   * RDA to DAMAP (Import).
   *
   * <ol>
   *   <li>First checks ID, title, and URL against the re3data API. If there is a match, imports as
   *       a repository.
   *   <li>Then checks title and URL against Internal Storage. If there is a match, imports as an
   *       internal storage.
   *   <li>If no match is found, imports as an external storage.
   * </ol>
   *
   * @param target the target DMP to import the host into
   * @param rdaHost the source RDA standard host
   * @param refHash the reference hash of the dataset linked to this host
   */
  public void importHost(DmpDO target, Host rdaHost, String refHash) {
    if (rdaHost == null) {
      return;
    }
    String title = !rdaHost.getTitle().isBlank() ? rdaHost.getTitle() : "Imported Host";
    String url = rdaHost.getUrl();

    // 1. Check against re3data API
    RepositoryDO matchedRepo = findRe3dataMatch(rdaHost);
    if (matchedRepo != null) {
      importAsRepository(target, matchedRepo, refHash);
      return;
    }

    // 2. Check against Internal Storage
    InternalStorage matchedInternal = findInternalStorageMatch(title, url);
    if (matchedInternal != null) {
      importAsInternalStorage(target, matchedInternal, title, refHash);
      return;
    }

    // 3. Fallback to External Storage
    importAsExternalStorage(target, title, url, refHash, rdaHost);
  }

  /**
   * Helper method to verify if an incoming RDA Host matches an entry in re3data.
   *
   * @param rdaHost the RDA standard host object to evaluate
   * @return a matched RepositoryDO if found, otherwise null
   */
  private RepositoryDO findRe3dataMatch(Host rdaHost) {
    String hostIdVal = null;
    if (rdaHost.getHostId() != null && !rdaHost.getHostId().isEmpty()) {
      hostIdVal = rdaHost.getHostId().get(0).getIdentifier();
    }

    RepositoriesService repositoriesService = null;
    try {
      repositoriesService = Arc.container().instance(RepositoriesService.class).get();
    } catch (Exception e) {
      log.debug("Could not obtain RepositoriesService from Arc container: " + e.getMessage());
    }

    if (repositoriesService == null) {
      return null;
    }

    // A. Check if explicitly provided via HostID with type re3data
    if (hostIdVal != null && !hostIdVal.isBlank()) {
      try {
        Re3Data re3Data = repositoriesService.getById(hostIdVal);
        if (re3Data != null && !re3Data.getRepository().isEmpty()) {
          var firstRepo = re3Data.getRepository().get(0);
          RepositoryDO repoDO = new RepositoryDO();
          repoDO.setRepositoryId(hostIdVal);
          String title = "Repository";
          if (firstRepo.getRepositoryName() != null
              && firstRepo.getRepositoryName().getValue() != null) {
            title = firstRepo.getRepositoryName().getValue();
          } else if (rdaHost.getTitle() != null) {
            title = rdaHost.getTitle();
          }
          repoDO.setTitle(title);
          return repoDO;
        }
      } catch (DamapApiException e) {
        EErrorCode errorCode = e.getPayload().errorCode();
        if (errorCode == EErrorCode.RE3DATA_NOT_FOUND) {
          log.warnv("Explicit hostId {0} not found in re3data registry during import", hostIdVal);
        } else if (errorCode == EErrorCode.RE3DATA_NOT_AVAILABLE) {
          log.warnv("re3data registry not available while checking explicit hostId {0}", hostIdVal);
        } else {
          log.errorv("Unexpected API error checking explicit hostId {0}", hostIdVal, e);
        }
      } catch (Exception e) {
        log.errorv("Unexpected exception checking re3data by explicit hostId {0}", hostIdVal, e);
      }
    }

    // B. Fallback: Search recommended repositories by Title or URL
    try {
      List<RepositoryDetails> recommendedList = repositoriesService.getRecommended();
      if (recommendedList != null) {
        for (RepositoryDetails recommended : recommendedList) {
          boolean matchByTitle =
              rdaHost.getTitle() != null
                  && rdaHost.getTitle().equalsIgnoreCase(recommended.getName());
          boolean matchByUrl =
              rdaHost.getUrl() != null
                  && !rdaHost.getUrl().isBlank()
                  && rdaHost.getUrl().equalsIgnoreCase(recommended.getRepositoryURL());

          if (matchByTitle || matchByUrl) {
            RepositoryDO repoDO = new RepositoryDO();
            repoDO.setRepositoryId(recommended.getId());
            repoDO.setTitle(recommended.getName());
            return repoDO;
          }
        }
      }
    } catch (DamapApiException e) {
      EErrorCode errorCode = e.getPayload().errorCode();
      if (errorCode == EErrorCode.RE3DATA_RECOMMENDED_NOT_FOUND) {
        log.warn("Recommended repositories mapping metadata not found", e);
      } else if (errorCode == EErrorCode.RE3DATA_RECOMMENDED_NOT_AVAILABLE) {
        log.warn("re3data service temporarily unavailable during recommended fallback lookup", e);
      } else {
        log.error(
            "Unexpected API error querying recommended repositories for import fallback match", e);
      }
    } catch (Exception e) {
      log.error(
          "Unexpected exception querying recommended repositories for import fallback match", e);
    }

    return null;
  }

  /**
   * Searches for a matching InternalStorage entity by comparing the host's title against active
   * localized storage translations, and its URL against active storage URLs.
   *
   * @param title the title of the host
   * @param url the URL of the host
   * @return the matching InternalStorage entity, or null if no match is found
   */
  private InternalStorage findInternalStorageMatch(String title, String url) {
    try {
      if (title != null && !title.isBlank()) {
        // Query the inter_storage_translation table using the exact 'title' column
        List<InternalStorageTranslation> translations =
            InternalStorageTranslation.list("LOWER(title) = LOWER(?1)", title.trim());

        for (InternalStorageTranslation translation : translations) {
          InternalStorage storage = translation.getInternalStorageId();
          if (storage != null && storage.isActive()) {
            return storage;
          }
        }
      }

      // 2. Fallback: Search active storages by homepage URL directly
      if (url != null && !url.isBlank()) {
        List<InternalStorage> activeStorages = InternalStorage.list("active", true);
        for (InternalStorage storage : activeStorages) {
          if (url.equalsIgnoreCase(storage.getUrl())) {
            return storage;
          }
        }
      }
    } catch (Exception e) {
      log.warn("Could not query InternalStorage translations or entities: " + e.getMessage());
    }
    return null;
  }

  /**
   * Helper method to import an RDA Host as an Internal Storage object. Creates a dedicated
   * StorageDO entry for each dataset link.
   *
   * @param target the target DMP
   * @param internal the matching internal storage entity
   * @param title the title of the internal storage entity
   * @param refHash the dataset's reference hash
   */
  private void importAsInternalStorage(
      DmpDO target, InternalStorage internal, String title, String refHash) {
    if (target.getStorage() == null) {
      target.setStorage(new ArrayList<>());
    }

    StorageDO storeDO = new StorageDO();
    storeDO.setInternalStorageId(internal.id);
    storeDO.setTitle(title != null && !title.isBlank() ? title : "Internal Storage");
    storeDO.setDatasets(new ArrayList<>(List.of(refHash)));
    target.getStorage().add(storeDO);
  }

  /**
   * Helper method to import an RDA Host as a DAMAP Repository object. Creates a dedicated
   * RepositoryDO entry for each dataset link.
   *
   * @param target the target DMP
   * @param matchedRepo the matched repository domain object
   * @param refHash the dataset's reference hash
   */
  private void importAsRepository(DmpDO target, RepositoryDO matchedRepo, String refHash) {
    if (target.getRepositories() == null) {
      target.setRepositories(new ArrayList<>());
    }

    RepositoryDO repoDO = new RepositoryDO();
    repoDO.setRepositoryId(matchedRepo.getRepositoryId());
    repoDO.setTitle(matchedRepo.getTitle());
    repoDO.setDatasets(new ArrayList<>(List.of(refHash)));
    target.getRepositories().add(repoDO);
  }

  /**
   * Helper method to import an RDA Host as a DAMAP External Storage object. Creates a dedicated
   * ExternalStorageDO entry for each dataset link.
   *
   * @param target the target DMP
   * @param title the host title
   * @param url the host url
   * @param refHash the dataset's reference hash
   * @param rdaHost the RDA host metadata source
   */
  private void importAsExternalStorage(
      DmpDO target, String title, String url, String refHash, Host rdaHost) {
    if (target.getExternalStorage() == null) {
      target.setExternalStorage(new ArrayList<>());
    }

    var newExt = new ExternalStorageDO();
    newExt.setTitle(title);
    newExt.setUrl(url);
    newExt.setDatasets(new ArrayList<>(List.of(refHash)));
    newExt.setBackupFrequency(rdaHost.getBackupFrequency());
    target.getExternalStorage().add(newExt);
  }
}
