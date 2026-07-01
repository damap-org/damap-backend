package org.damap.base.rda.dmpcommonstandard;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.damap.base.enums.*;
import org.damap.base.rest.dmp.domain.DatasetDO;
import org.damap.base.rest.dmp.domain.IdentifierDO;

/**
 * This class implements Dataset conversion from and to the RDA DMP common standard. (See <a
 * href="https://github.com/RDA-DMP-Common/common-madmp-api">github.com/RDA-DMP-Common/common-madmp-api</a>
 * )
 *
 * <p>The conversion from the common standard into DAMAP objects is best-effort since not all data
 * can be represented.
 */
public class DatasetMapper extends AbstractMapper {
  /** Initialize the mapper with the default settings (strict mode turned on). */
  public DatasetMapper() {
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
  public DatasetMapper(boolean strict) {
    super(strict);
  }

  public Dataset convert(DatasetDO datasetDO) {
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
    var fileFormat = datasetDO.getFileFormat();
    var distribution = new Distribution();
    distribution.setTitle(
        datasetDO.getTitle() != null ? datasetDO.getTitle() : "Dataset Distribution");
    if (fileFormat != null && !fileFormat.isEmpty()) {
      distribution = distribution.format(Collections.singletonList(fileFormat));
    }
    var size = datasetDO.getSize();
    if (size != null) {
      distribution.setByteSize(size);
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
    result.setDistribution(Collections.singletonList(distribution));

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
