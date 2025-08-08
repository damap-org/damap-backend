package org.damap.base.rda.dmpcommonstandard;

import java.util.Collections;
import java.util.stream.Collectors;
import org.damap.base.enums.EDataAccessType;
import org.damap.base.enums.EDataType;
import org.damap.base.enums.EIdentifierType;
import org.damap.base.enums.ELicense;
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
    if (datasetId != null) {
      result.setDatasetId(
          new DatasetID()
              .identifier(datasetId.getIdentifier())
              .type(
                  switch (datasetId.getType()) {
                    case ARK -> DatasetIDType.ARK;
                    case HANDLE -> DatasetIDType.HANDLE;
                    case DOI -> DatasetIDType.DOI;
                    case URL -> DatasetIDType.URL;
                    default -> DatasetIDType.OTHER;
                  }));
    } else {
      result.setDatasetId(
          new DatasetID().type(DatasetIDType.OTHER).identifier(String.valueOf(datasetDO.getId())));
    }

    result.setTitle(datasetDO.getTitle());
    result.setType(
        datasetDO.getType().stream().map(EDataType::toString).collect(Collectors.joining(", ")));
    var fileFormat = datasetDO.getFileFormat();
    var distribution = new Distribution();
    result.setDistribution(Collections.singletonList(distribution));
    if (fileFormat != null && !fileFormat.isEmpty()) {
      distribution = distribution.format(Collections.singletonList(fileFormat));
    }
    var size = datasetDO.getSize();
    if (size != null) {
      distribution.setByteSize(Math.toIntExact(size));
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
      distribution.setLicense(
          Collections.singletonList(new License().licenseRef(license.getAcronym())));
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

    var trs = result.getTechnicalResource();
    if (trs != null) {
      result.setTechnicalResource(
          trs.stream()
              .map(
                  tr -> new TechnicalResource().description(tr.getDescription()).name(tr.getName()))
              .collect(Collectors.toList()));
    }
    return result;
  }

  public DatasetDO convert(Dataset dataset) {
    var result = new DatasetDO();

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
    datasetId.setType(
        switch (dataset.getDatasetId().getType()) {
          case ARK -> EIdentifierType.ARK;
          case DOI -> EIdentifierType.DOI;
          case URL -> EIdentifierType.URL;
          case HANDLE -> EIdentifierType.HANDLE;
          default -> EIdentifierType.OTHER;
        });
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
      if (format != null) {
        result.setFileFormat(format.get(0));
      }
      var license = distribution.getLicense();
      if (license != null && !license.isEmpty()) {
        try {
          result.setLicense(ELicense.valueOf(license.get(0).getLicenseRef()));
        } catch (IllegalArgumentException e) {
          if (strict) {
            throw new CommonStandardCompatibilityException(
                "unsupported license: " + license.get(0).getLicenseRef());
          }
          result.setLicense(ELicense.CUSTOM);
        }
      }
      var dataAccess = distribution.getDataAccess();
      if (dataAccess == DataAccess.SHARED && strict) {
        throw new CommonStandardCompatibilityException(
            "DAMAP does not support the 'shared' data access");
      }
      result.setDataAccess(
          switch (dataAccess) {
            case OPEN -> EDataAccessType.OPEN;
            case CLOSED -> EDataAccessType.CLOSED;
            case SHARED -> null;
          });
    }

    return result;
  }
}
