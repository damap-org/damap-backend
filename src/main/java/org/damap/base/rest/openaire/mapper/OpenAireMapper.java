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
    dataset.setTitle(truncate(preferredLocalizedValue(product.getTitles()), MAX_TITLE_LENGTH));
    dataset.setDescription(joinPreferredLocalizedValues(product.getAbstracts(), " "));
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
            mapType(preferredLocalizedLabel(manifestation.getType().getLabels())),
            dataset.getType());
      }
      if (dataset.getLicense() == null) {
        dataset.setLicense(mapLicense(manifestation.getLicence()));
      }
      if (dataset.getDataAccess() == null) {
        dataset.setDataAccess(mapAccessRight(manifestation.getAccessRights()));
      }
      if (manifestation.getDates() != null) {
        Date manifestationDate = earliestManifestationDate(manifestation);
        if (manifestationDate != null
            && (dataset.getStartDate() == null
                || manifestationDate.before(dataset.getStartDate()))) {
          dataset.setStartDate(manifestationDate);
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
      case "closed", "unavailable" -> EDataAccessType.CLOSED;
      default -> null;
    };
  }

  private EDataType mapType(String value) {
    if (value == null || value.isBlank()) {
      return EDataType.OTHER;
    }
    String type = value.trim().toLowerCase(Locale.ROOT);
    if (type.equals("literature")) return EDataType.PLAIN_TEXT;
    if (type.equals("research software")) return EDataType.SOFTWARE_APPLICATIONS;
    if (type.equals("research data") || type.equals("other")) return EDataType.OTHER;
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
    // OpenAIRE may return these aliases, while ELicense recognizes CC0-1.0 and its URL.
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

  private Date earliestManifestationDate(OpenAireManifestation manifestation) {
    OpenAireAccessRights accessRights = manifestation.getAccessRights();
    if (accessRights != null && accessRights.getStatus() != null) {
      String status = accessRights.getStatus().trim().toLowerCase(Locale.ROOT);
      if (status.equals("embargo") || status.equals("embargoed")) {
        Date embargoDate = earliestDate(manifestation.getDates().getEmbargo());
        if (embargoDate != null) return embargoDate;
      }
    }
    return earliestDate(manifestation.getDates().getPublication());
  }

  private Date parseDate(String value) {
    if (value == null || value.isBlank()) return null;
    try {
      return Date.from(LocalDate.parse(value).atStartOfDay().toInstant(ZoneOffset.UTC));
    } catch (DateTimeParseException ignored) {
      return null;
    }
  }

  private String joinPreferredLocalizedValues(Map<String, List<String>> values, String delimiter) {
    List<String> localizedValues = preferredLocalizedValues(values);
    if (localizedValues == null) return null;
    return localizedValues.stream()
        .filter(Objects::nonNull)
        .map(String::trim)
        .filter(value -> !value.isEmpty())
        .distinct()
        .reduce((first, second) -> first + delimiter + second)
        .orElse(null);
  }

  private List<String> preferredLocalizedValues(Map<String, List<String>> values) {
    if (values == null || values.isEmpty()) return null;
    List<String> english = values.get("en");
    if (firstNonBlank(english) != null) return english;
    List<String> none = values.get("none");
    return firstNonBlank(none) != null
        ? none
        : values.values().stream()
            .filter(localizedValues -> firstNonBlank(localizedValues) != null)
            .findFirst()
            .orElse(null);
  }

  private String preferredLocalizedLabel(Map<String, String> values) {
    if (values == null || values.isEmpty()) return null;
    String english = trimToNull(values.get("en"));
    if (english != null) return english;
    String none = trimToNull(values.get("none"));
    return none != null
        ? none
        : values.values().stream()
            .map(OpenAireMapper::trimToNull)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
  }

  private String preferredLocalizedValue(Map<String, List<String>> values) {
    return firstNonBlank(preferredLocalizedValues(values));
  }

  private String firstNonBlank(List<String> values) {
    if (values == null) return null;
    return values.stream()
        .map(OpenAireMapper::trimToNull)
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
  }

  private String trimToNull(String value) {
    if (value == null || value.isBlank()) return null;
    return value.trim();
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
