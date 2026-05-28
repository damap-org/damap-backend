package org.damap.base.rda.dmpcommonstandard;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.damap.base.enums.*;
import org.damap.base.rest.dmp.domain.DatasetDO;
import org.damap.base.rest.dmp.domain.DmpDO;
import org.damap.base.rest.dmp.domain.ExternalStorageDO;
import org.damap.base.rest.dmp.domain.IdentifierDO;
import org.damap.base.rest.dmp.domain.RepositoryDO;
import org.damap.base.rest.dmp.domain.StorageDO;
import org.damap.base.rest.dmp.mapper.MapperService;
import org.damap.base.rest.storage.InternalStorageDO;
import org.damap.base.rest.storage.InternalStorageTranslationDO;
import org.re3data.schema._2_2.Certificates;
import org.re3data.schema._2_2.PidSystems;
import org.re3data.schema._2_2.Re3Data;
import org.re3data.schema._2_2.Yesno;

/**
 * This class implements Dataset conversion from and to the RDA DMP common standard. (See <a
 * href="https://github.com/RDA-DMP-Common/common-madmp-api">github.com/RDA-DMP-Common/common-madmp-api</a>
 * )
 *
 * <p>The conversion from the common standard into DAMAP objects is best-effort since not all data
 * can be represented.
 */
public class DatasetMapper extends AbstractMapper {

  private static final String DEFAULT_LANGUAGE_CODE = "eng";

  private final MapperService mapperService;

  /** Initialize the mapper with the default settings (strict mode turned on). */
  public DatasetMapper() {
    this(true, null);
  }

  /**
   * Initialize the mapper with an option to turn off strict mode. Only use non-strict mode when a
   * human has to review the DMP afterward, never use it for automated imports!
   *
   * @param strict When enabled, any dropped data will cause a {@link
   *     CommonStandardCompatibilityException}. When disabled, DAMAP will do its best to represent
   *     any imported data in the common format but drop data it cannot represent.
   */
  public DatasetMapper(boolean strict) {
    this(strict, null);
  }

  /**
   * Initialize the mapper with strict mode plus a {@link MapperService} used to resolve hosts
   * (repository metadata via re3data, internal storage details, etc.). When the service is null the
   * mapper still produces output, but distributions will not carry a {@code host} object.
   */
  public DatasetMapper(boolean strict, MapperService mapperService) {
    super(strict);
    this.mapperService = mapperService;
  }

  /**
   * Convert a single dataset without DMP context. Distributions emitted by this overload will not
   * carry a {@code host} (the host information lives on the parent DMP). Prefer {@link
   * #convert(DmpDO, DatasetDO)} when the parent DMP is available.
   */
  public Dataset convert(DatasetDO datasetDO) {
    return convert(null, datasetDO);
  }

  public Dataset convert(DmpDO dmp, DatasetDO datasetDO) {
    var result = new Dataset();

    var datasetId = datasetDO.getDatasetId();
    if (datasetId != null && datasetId.getIdentifier() != null) {
      DatasetID rdaDatasetId = new DatasetID().identifier(datasetId.getIdentifier());
      if (datasetId.getType() != null) {
        rdaDatasetId.setType(datasetId.getType().toString().toLowerCase());
      } else {
        rdaDatasetId.setType("other");
      }
      result.setDatasetId(rdaDatasetId);
    } else {
      result.setDatasetId(
          new DatasetID().type("other").identifier(String.valueOf(datasetDO.getId())));
    }

    result.setTitle(
        datasetDO.getTitle() != null && !datasetDO.getTitle().isBlank()
            ? datasetDO.getTitle()
            : "Untitled Dataset");
    if (datasetDO.getType() != null && !datasetDO.getType().isEmpty()) {
      result.setType(
          datasetDO.getType().stream().map(EDataType::toString).collect(Collectors.joining(", ")));
    }
    result.setDescription(datasetDO.getDescription());
    var personalData = datasetDO.getPersonalData();
    if (personalData == null) {
      result.setPersonalData(Dataset.PersonalDataEnum.UNKNOWN);
    } else if (personalData) {
      result.setPersonalData(Dataset.PersonalDataEnum.YES);
    } else {
      result.setPersonalData(Dataset.PersonalDataEnum.NO);
    }
    var sensitiveData = datasetDO.getSensitiveData();
    if (sensitiveData == null) {
      result.setSensitiveData(Dataset.SensitiveDataEnum.UNKNOWN);
    } else if (sensitiveData) {
      result.setSensitiveData(Dataset.SensitiveDataEnum.YES);
    } else {
      result.setSensitiveData(Dataset.SensitiveDataEnum.NO);
    }
    List<Distribution> distributions = buildDistributions(dmp, datasetDO);
    result.setDistribution(distributions);

    var trs = datasetDO.getTechnicalResources();
    if (trs != null && !trs.isEmpty()) {
      result.setTechnicalResource(
          trs.stream()
              .map(
                  tr -> {
                    TechnicalResource rdaTr = new TechnicalResource();
                    String name = tr.getName();
                    rdaTr.setName(name != null && !name.isBlank() ? name : "Unspecified Resource");
                    rdaTr.setDescription(tr.getDescription());

                    return rdaTr;
                  })
              .collect(Collectors.toList()));
    }
    return result;
  }

  private List<Distribution> buildDistributions(DmpDO dmp, DatasetDO datasetDO) {
    List<Distribution> distributions = new ArrayList<>();
    String referenceHash = datasetDO.getReferenceHash();

    if (dmp != null && referenceHash != null) {
      if (dmp.getRepositories() != null) {
        dmp.getRepositories().stream()
            .filter(r -> r.getDatasets() != null && r.getDatasets().contains(referenceHash))
            .forEach(
                repositoryDO -> {
                  Distribution distribution = newBaseDistribution(datasetDO);
                  attachRepositoryHost(distribution, repositoryDO);
                  if (distribution.getHost() != null) {
                    distributions.add(distribution);
                  }
                });
      }
      if (dmp.getStorage() != null) {
        dmp.getStorage().stream()
            .filter(s -> s.getDatasets() != null && s.getDatasets().contains(referenceHash))
            .forEach(
                storageDO -> {
                  Distribution distribution = newBaseDistribution(datasetDO);
                  attachInternalStorageHost(distribution, storageDO);
                  if (distribution.getHost() != null) {
                    distributions.add(distribution);
                  }
                });
      }
      if (dmp.getExternalStorage() != null) {
        dmp.getExternalStorage().stream()
            .filter(s -> s.getDatasets() != null && s.getDatasets().contains(referenceHash))
            .forEach(
                externalStorageDO -> {
                  Distribution distribution = newBaseDistribution(datasetDO);
                  attachExternalStorageHost(distribution, externalStorageDO);
                  if (distribution.getHost() != null) {
                    distributions.add(distribution);
                  }
                });
      }
    }

    if (distributions.isEmpty()) {
      // No host attached to this dataset. Fall back to a single host-less distribution so the
      // produced DMP still describes the dataset's licensing/access/format.
      distributions.add(newBaseDistribution(datasetDO));
    }

    return distributions;
  }

  private Distribution newBaseDistribution(DatasetDO datasetDO) {
    Distribution distribution = new Distribution();
    distribution.setTitle(
        datasetDO.getTitle() != null && !datasetDO.getTitle().isBlank()
            ? datasetDO.getTitle()
            : "Dataset Distribution");

    String fileFormat = datasetDO.getFileFormat();
    if (fileFormat != null && !fileFormat.isEmpty()) {
      distribution.setFormat(Collections.singletonList(fileFormat));
    }

    Long size = datasetDO.getSize();
    if (size != null) {
      distribution.setByteSize(Math.toIntExact(size));
    }

    var license = datasetDO.getLicense();
    if (license != null) {
      License rdaLicense = new License();
      String ref = license.getUrl();
      if (ref == null || ref.isBlank()) {
        ref = license.getAcronym();
      }
      rdaLicense.setLicenseRef(
          ref != null && !ref.isBlank() ? ref : "https://example.org/unknown-license");
      if (datasetDO.getStartDate() != null) {
        rdaLicense.setStartDate(
            datasetDO.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
      } else {
        rdaLicense.setStartDate(LocalDate.now());
      }
      distribution.setLicense(Collections.singletonList(rdaLicense));
    }

    var dataAccess = datasetDO.getDataAccess();
    if (dataAccess != null) {
      distribution.setDataAccess(
          switch (dataAccess) {
            case CLOSED, RESTRICTED -> DataAccess.CLOSED;
            case OPEN -> DataAccess.OPEN;
          });
    } else {
      if (strict) {
        throw new CommonStandardCompatibilityException(
            "cannot produce data access (none present in DMP)");
      }
      distribution.setDataAccess(DataAccess.CLOSED);
    }
    return distribution;
  }

  private void attachRepositoryHost(Distribution distribution, RepositoryDO repositoryDO) {
    if (mapperService == null || repositoryDO.getRepositoryId() == null) {
      return;
    }
    Re3Data.Repository repository;
    try {
      repository = mapperService.getRe3DataRepository(repositoryDO.getRepositoryId());
    } catch (RuntimeException e) {
      // re3data lookup failed (network, cache miss, malformed id). Don't fail the whole export.
      return;
    }
    if (repository == null) {
      return;
    }

    Host host = new Host();
    if (repository.getRepositoryName() != null) {
      host.setTitle(repository.getRepositoryName().getValue());
    } else if (repositoryDO.getTitle() != null) {
      host.setTitle(repositoryDO.getTitle());
    } else {
      host.setTitle("Repository");
    }
    if (repository.getRepositoryURL() != null) {
      host.setUrl(repository.getRepositoryURL());
      distribution.setAccessUrl(repository.getRepositoryURL());
    }
    if (repository.getDescription() != null) {
      host.setDescription(repository.getDescription().getValue());
    }
    host.setCertifiedWith(toCertification(repository.getCertificate()));
    host.setPidSystem(
        repository.getPidSystem().stream()
            .map(this::toPidSystem)
            .filter(p -> p != null)
            .collect(Collectors.toList()));
    repository.getType().stream()
        .findFirst()
        .ifPresent(rt -> host.setStorageType(rt.value()));
    host.setSupportVersioning(toSupportVersioning(repository.getVersioning()));

    if (host.getUrl() != null) {
      distribution.setHost(host);
    }
  }

  private void attachInternalStorageHost(Distribution distribution, StorageDO storageDO) {
    if (mapperService == null || storageDO.getInternalStorageId() == null) {
      return;
    }
    InternalStorageDO internalStorage =
        mapperService.getInternalStorageDOById(
            storageDO.getInternalStorageId(), DEFAULT_LANGUAGE_CODE);
    if (internalStorage == null || internalStorage.getUrl() == null) {
      return;
    }

    Host host = new Host();
    host.setUrl(internalStorage.getUrl());
    distribution.setAccessUrl(internalStorage.getUrl());

    InternalStorageTranslationDO translation = pickTranslation(internalStorage);
    if (translation != null) {
      host.setTitle(
          translation.getTitle() != null && !translation.getTitle().isBlank()
              ? translation.getTitle()
              : "Internal Storage");
      host.setDescription(translation.getDescription());
      host.setBackupFrequency(translation.getBackupFrequency());
    } else {
      host.setTitle(storageDO.getTitle() != null ? storageDO.getTitle() : "Internal Storage");
    }

    distribution.setHost(host);
  }

  private void attachExternalStorageHost(
      Distribution distribution, ExternalStorageDO externalStorageDO) {
    if (externalStorageDO.getUrl() == null || externalStorageDO.getUrl().isBlank()) {
      return;
    }
    Host host = new Host();
    host.setUrl(externalStorageDO.getUrl());
    host.setTitle(
        externalStorageDO.getTitle() != null && !externalStorageDO.getTitle().isBlank()
            ? externalStorageDO.getTitle()
            : "External Storage");
    host.setBackupFrequency(externalStorageDO.getBackupFrequency());

    distribution.setAccessUrl(externalStorageDO.getUrl());
    distribution.setHost(host);
  }

  private InternalStorageTranslationDO pickTranslation(InternalStorageDO internalStorage) {
    List<InternalStorageTranslationDO> translations = internalStorage.getTranslations();
    if (translations == null || translations.isEmpty()) {
      return null;
    }
    for (InternalStorageTranslationDO t : translations) {
      if (DEFAULT_LANGUAGE_CODE.equals(t.getLanguageCode())) {
        return t;
      }
    }
    return translations.get(0);
  }

  private Certification toCertification(List<Certificates> certificates) {
    if (certificates == null) {
      return null;
    }
    for (Certificates certificate : certificates) {
      switch (certificate) {
        case DIN_31644:
          return Certification.DIN31644;
        case DINI_CERTIFICATE:
          return Certification.DINI_ZERTIFIKAT;
        case DSA:
          return Certification.DSA;
        case ISO_16363:
          return Certification.ISO16363;
        case ISO_16919:
          return Certification.ISO16919;
        case TRAC:
          return Certification.TRAC;
        case WDS:
          return Certification.WDS;
        case CLARIN_CERTIFICATE_B:
        case DRAMBORA:
        case RAT_SWD:
        case TRUSTED_DIGITAL_REPOSITORY:
        case OTHER:
          // not representable in the RDA Certification enum
      }
    }
    return null;
  }

  private PIDSystemType toPidSystem(PidSystems pidSystem) {
    if (pidSystem == null) {
      return null;
    }
    return switch (pidSystem) {
      case ARK -> PIDSystemType.ARK;
      case DOI -> PIDSystemType.DOI;
      case HDL -> PIDSystemType.HANDLE;
      case PURL -> PIDSystemType.PURL;
      case URN -> PIDSystemType.URN;
      case OTHER -> PIDSystemType.OTHER;
      case NONE -> null;
    };
  }

  private Booleanish toSupportVersioning(Yesno versioning) {
    if (versioning == Yesno.YES) {
      return Booleanish.YES;
    }
    if (versioning == Yesno.NO) {
      return Booleanish.NO;
    }
    return Booleanish.UNKNOWN;
  }

  public DatasetDO convert(Dataset dataset) {
    var result = new DatasetDO();
    result.setSource(EDataSource.NEW);
    result.setTitle(dataset.getTitle());

    if (strict) {
      if (dataset.getDataQualityAssurance() != null
          && !dataset.getDataQualityAssurance().isEmpty()) {
        throw new CommonStandardCompatibilityException(
            "Data quality assurance objects are not supported in DAMAP.");
      }
      if (dataset.getIsReused() != null) {
        throw new CommonStandardCompatibilityException(
            "Reused dataset markers are not supported in DAMAP.");
      }
      if (dataset.getIssued() != null) {
        throw new CommonStandardCompatibilityException(
            "Dataset issued dates are not supported in DAMAP.");
      }
      if (dataset.getKeyword() != null && !dataset.getKeyword().isEmpty()) {
        throw new CommonStandardCompatibilityException(
            "Dataset keywords are not supported in DAMAP.");
      }
    }

    var datasetId = new IdentifierDO();
    datasetId.setIdentifier(dataset.getDatasetId().getIdentifier());
    if (dataset.getDatasetId().getType() != null) {
      datasetId.setType(
          switch (dataset.getDatasetId().getType().toLowerCase()) {
            case "ark" -> EIdentifierType.ARK;
            case "doi" -> EIdentifierType.DOI;
            case "url" -> EIdentifierType.URL;
            case "handle" -> EIdentifierType.HANDLE;
            default -> EIdentifierType.OTHER;
          });
    } else {
      datasetId.setType(EIdentifierType.OTHER);
    }
    result.setDatasetId(datasetId);

    result.setDescription(dataset.getDescription());

    var distributions = dataset.getDistribution();
    if (distributions != null && !distributions.isEmpty()) {
      var distribution = distributions.get(0);
      if (strict) {
        if (distributions.size() > 1) {
          throw new CommonStandardCompatibilityException(
              "Multiple distribution objects are not supported in DAMAP.");
        }
        if (distribution.getAccessUrl() != null && !distribution.getAccessUrl().isEmpty()) {
          throw new CommonStandardCompatibilityException(
              "Access URLs on distribution objects are not supported in DAMAP.");
        }
        if (distribution.getAvailableUntil() != null) {
          throw new CommonStandardCompatibilityException(
              "Dataset availability on distribution objects is not supported in DAMAP.");
        }
        if (distribution.getHost() != null) {
          throw new CommonStandardCompatibilityException(
              "Hosts on distribution objects are not supported in DAMAP.");
        }
        if (distribution.getDownloadUrl() != null && !distribution.getDownloadUrl().isEmpty()) {
          throw new CommonStandardCompatibilityException(
              "Download URLs on distribution objects are not supported in DAMAP.");
        }
        if (distribution.getLicense() != null && distribution.getLicense().size() > 1) {
          throw new CommonStandardCompatibilityException(
              "Multiple licenses on distribution objects are not supported in DAMAP.");
        }
        if (distribution.getLicense() != null
            && !distribution.getLicense().isEmpty()
            && distribution.getLicense().get(0).getStartDate() != null) {
          // TODO this will always fail if there is a license since the start_date is a required
          // field.
          throw new CommonStandardCompatibilityException(
              "DAMAP does not support recording the start date of licenses.");
        }
        if (distribution.getFormat() != null && distribution.getFormat().size() > 1) {
          throw new CommonStandardCompatibilityException(
              "Multiple formats on distribution objects are not supported in DAMAP.");
        }
      }
      var size = distribution.getByteSize();
      if (size != null) {
        result.setSize(Long.valueOf(size));
      }
      var format = distribution.getFormat();
      if (format != null && !format.isEmpty()) {
        result.setFileFormat(format.get(0));
      }
      var license = distribution.getLicense();
      if (license != null && !license.isEmpty()) {
        String ref = license.get(0).getLicenseRef();
        ELicense eLicense = ELicense.getByAcronymOrUrl(ref);
        if (eLicense != null) {
          result.setLicense(eLicense);
        } else {
          try {
            result.setLicense(ELicense.valueOf(ref));
          } catch (IllegalArgumentException e) {
            if (strict) {
              throw new CommonStandardCompatibilityException("unsupported license: " + ref);
            }
            result.setLicense(ELicense.CUSTOM);
          }
        }
      }
      var dataAccess = distribution.getDataAccess();
      if (dataAccess != null) {
        if (dataAccess == DataAccess.SHARED && strict) {
          throw new CommonStandardCompatibilityException(
              "DAMAP does not support the 'shared' data access");
        }
        result.setDataAccess(
            switch (dataAccess) {
              case OPEN -> EDataAccessType.OPEN;
              case CLOSED -> EDataAccessType.CLOSED;
              case SHARED -> EDataAccessType.RESTRICTED;
            });
      }
    }

    if (dataset.getPersonalData() != null) {
      result.setPersonalData(dataset.getPersonalData() == Dataset.PersonalDataEnum.YES);
    }
    if (dataset.getSensitiveData() != null) {
      result.setSensitiveData(dataset.getSensitiveData() == Dataset.SensitiveDataEnum.YES);
    }

    if (dataset.getType() != null) {
      try {
        result.setType(List.of(EDataType.valueOf(dataset.getType().toUpperCase())));
      } catch (IllegalArgumentException e) {
        // best-effort: DamapDO expects an array, but rda common standard provides string
      }
    }

    if (dataset.getTechnicalResource() != null && !dataset.getTechnicalResource().isEmpty()) {
      result.setTechnicalResources(
          dataset.getTechnicalResource().stream()
              .map(
                  tr -> {
                    var trDO = new org.damap.base.rest.dmp.domain.TechnicalResourceDO();
                    trDO.setName(tr.getName());
                    trDO.setDescription(tr.getDescription());
                    return trDO;
                  })
              .collect(Collectors.toList()));
    }
    return result;
  }
}
