package org.damap.base.rest.openaire.mapper;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import lombok.experimental.UtilityClass;
import org.damap.base.enums.EAccessRight;
import org.damap.base.enums.EDataAccessType;
import org.damap.base.enums.EDataSource;
import org.damap.base.enums.EDataType;
import org.damap.base.enums.EIdentifierType;
import org.damap.base.enums.ELicense;
import org.damap.base.rest.dmp.domain.DatasetDO;
import org.damap.base.rest.dmp.domain.IdentifierDO;
import org.damap.base.rest.openaire.domain.OpenAireAccessRights;
import org.damap.base.rest.openaire.domain.OpenAireManifestation;
import org.damap.base.rest.openaire.domain.OpenAireProduct;

/** Maps OpenAIRE SKG-IF products to DAMAP datasets. */
@UtilityClass
public class OpenAireMapper {

  private static final int MAX_TITLE_LENGTH = 255;

  /** Map an OpenAIRE SKG-IF product to a new DAMAP dataset. */
  public DatasetDO map(String doi, OpenAireProduct product) {
    Objects.requireNonNull(product, "OpenAIRE research product must not be null");

    DatasetDO dataset = new DatasetDO();
    dataset.setSource(EDataSource.REUSED);
    dataset.setTitle(truncate(firstLocalizedListValue(product.getTitles()), MAX_TITLE_LENGTH));
    dataset.setDescription(joinLocalizedValues(product.getAbstracts(), " "));
    dataset.setDatasetId(createDoiIdentifier(doi));
    dataset.setSelectedProjectMembersAccess(EAccessRight.READ);
    dataset.setOtherProjectMembersAccess(EAccessRight.READ);
    dataset.setPublicAccess(EAccessRight.READ);

    mapManifestations(product.getManifestations(), dataset);
    if (dataset.getType().isEmpty()) {
      addType(mapType(product.getProductType()), dataset.getType());
    }

    return dataset;
  }

  private void mapManifestations(List<OpenAireManifestation> manifestations, DatasetDO dataset) {
    if (manifestations == null) {
      return;
    }

    for (OpenAireManifestation manifestation : manifestations) {
      if (manifestation == null) {
        continue;
      }
      if (manifestation.getType() != null) {
        addType(
            mapType(firstLocalizedValue(manifestation.getType().getLabels())), dataset.getType());
      }
      if (dataset.getLicense() == null) {
        dataset.setLicense(mapLicense(manifestation.getLicence()));
      }
      if (dataset.getDataAccess() == null) {
        dataset.setDataAccess(mapAccessRight(manifestation.getAccessRights()));
      }
      if (manifestation.getDates() != null) {
        Date publicationDate = earliestDate(manifestation.getDates().getPublication());
        if (publicationDate != null
            && (dataset.getStartDate() == null || publicationDate.before(dataset.getStartDate()))) {
          dataset.setStartDate(publicationDate);
        }
      }
    }
  }

  private EDataAccessType mapAccessRight(OpenAireAccessRights accessRights) {
    if (accessRights == null || accessRights.getStatus() == null) {
      return null;
    }
    return switch (accessRights.getStatus().trim().toLowerCase(Locale.ROOT)) {
      case "open" -> EDataAccessType.OPEN;
      case "restricted", "embargo", "embargoed" -> EDataAccessType.RESTRICTED;
      case "closed" -> EDataAccessType.CLOSED;
      default -> null;
    };
  }

  private EDataType mapType(String value) {
    if (value == null || value.isBlank()) {
      return EDataType.OTHER;
    }
    String type = value.toLowerCase(Locale.ROOT);
    if (type.contains("image")) return EDataType.IMAGES;
    if (type.contains("audio")
        || type.contains("video")
        || type.contains("film")
        || type.contains("sound")) return EDataType.AUDIOVISUAL_DATA;
    if (type.contains("source code")) return EDataType.SOURCE_CODE;
    if (type.contains("software") || type.contains("application"))
      return EDataType.SOFTWARE_APPLICATIONS;
    if (type.contains("database")) return EDataType.DATABASES;
    if (type.contains("text")
        || type.contains("article")
        || type.contains("publication")
        || type.contains("book")
        || type.contains("thesis")
        || type.contains("preprint")) return EDataType.PLAIN_TEXT;
    return EDataType.OTHER;
  }

  private ELicense mapLicense(String value) {
    if (value == null || value.isBlank()) return null;
    String normalized = value.trim();
    if ("CC 0".equalsIgnoreCase(normalized) || "CC0".equalsIgnoreCase(normalized)) {
      return ELicense.CCZERO;
    }
    return ELicense.getByAcronymOrUrl(normalized);
  }

  private Date earliestDate(List<String> values) {
    if (values == null) return null;
    return values.stream()
        .map(OpenAireMapper::parseDate)
        .filter(Objects::nonNull)
        .min(Date::compareTo)
        .orElse(null);
  }

  private Date parseDate(String value) {
    if (value == null || value.isBlank()) return null;
    try {
      return Date.from(LocalDate.parse(value).atStartOfDay().toInstant(ZoneOffset.UTC));
    } catch (DateTimeParseException ignored) {
      return null;
    }
  }

  private String joinLocalizedValues(Map<String, List<String>> values, String delimiter) {
    if (values == null) return null;
    return values.values().stream()
        .filter(Objects::nonNull)
        .flatMap(List::stream)
        .filter(Objects::nonNull)
        .map(String::trim)
        .filter(value -> !value.isEmpty())
        .distinct()
        .reduce((first, second) -> first + delimiter + second)
        .orElse(null);
  }

  private <T> T firstLocalizedValue(Map<String, T> values) {
    if (values == null || values.isEmpty()) return null;
    T none = values.get("none");
    return none != null
        ? none
        : values.values().stream().filter(Objects::nonNull).findFirst().orElse(null);
  }

  private String firstLocalizedListValue(Map<String, List<String>> values) {
    List<String> localizedValues = firstLocalizedValue(values);
    return localizedValues == null
        ? null
        : localizedValues.stream().filter(Objects::nonNull).findFirst().orElse(null);
  }

  private String truncate(String value, int maximumLength) {
    if (value == null || value.length() <= maximumLength) return value;
    return value.substring(0, maximumLength);
  }

  private IdentifierDO createDoiIdentifier(String doi) {
    IdentifierDO identifier = new IdentifierDO();
    identifier.setType(EIdentifierType.DOI);
    identifier.setIdentifier(doi);
    return identifier;
  }

  private void addType(EDataType type, List<EDataType> types) {
    if (!types.contains(type)) types.add(type);
  }
}
