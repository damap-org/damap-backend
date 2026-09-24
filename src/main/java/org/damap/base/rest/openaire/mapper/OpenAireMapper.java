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

  /**
   * Maps an OpenAIRE research product to a dataset.
   *
   * @param doi DOI of the research product
   * @param product OpenAIRE research product to map
   * @return the mapped {@link DatasetDO}
   * @throws NullPointerException if the product is {@code null}
   */
  public DatasetDO map(String doi, OpenAireProduct product) {
    Objects.requireNonNull(product, "OpenAIRE research product must not be null");

    DatasetDO dataset = new DatasetDO();
    dataset.setSource(EDataSource.REUSED);
    dataset.setTitle(
        truncate(firstNonBlank(preferredLocalizedValues(product.getTitles())), MAX_TITLE_LENGTH));
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

  /**
   * Maps manifestation metadata to the dataset, including type, license, data access, and the start
   * date.
   *
   * @param manifestations manifestations to map
   * @param dataset dataset to update
   */
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

  /**
   * Maps SKG-IF access rights to DAMAP {@link EDataAccessType}
   *
   * @param accessRights access rights to map
   * @return {@link EDataAccessType} mapped from input access rights
   */
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

  /**
   * Maps SKG-IF dataset types to DAMAP {@link EDataType}
   *
   * @param value data type string to map
   * @return {@link EDataType} mapped from input data type
   */
  private EDataType mapType(String value) {
    if (value == null || value.isBlank()) {
      return EDataType.OTHER;
    }
    String type = value.trim().toLowerCase(Locale.ROOT);
    if (type.contains("research data") || type.contains("other")) return EDataType.OTHER;
    if (type.contains("image")) return EDataType.IMAGES;
    if (type.contains("audio")
        || type.contains("video")
        || type.contains("film")
        || type.contains("sound")) return EDataType.AUDIOVISUAL_DATA;
    if (type.contains("source code")) return EDataType.SOURCE_CODE;
    if (type.contains("software")
        || type.contains("application")
        || type.contains("research software")) return EDataType.SOFTWARE_APPLICATIONS;
    if (type.contains("database")) return EDataType.DATABASES;
    if (type.contains("text")
        || type.contains("article")
        || type.contains("publication")
        || type.contains("book")
        || type.contains("thesis")
        || type.contains("preprint")
        || type.contains("literature")) return EDataType.PLAIN_TEXT;
    return EDataType.OTHER;
  }

  /**
   * Maps SKG-IF license to DAMAP {@link ELicense}
   *
   * @param value license string to map
   * @return {@link ELicense} mapped from input license
   */
  private ELicense mapLicense(String value) {
    if (value == null || value.isBlank()) return null;
    String normalized = value.trim();
    // OpenAire harmonizes licenses like described in this document
    // https://api.openaire.eu/vocabularies/dnet:licenses
    // The harmonization is very broad, e.g. AGPL-3.0, AGPL-3.0-only and AGPL-3.0-or-later are all
    // harmonized to AGPL
    // TODO: Find out what this means for extracting the licensing information, as we will get wrong
    // information
    // E.g. if we get CC-BY from OpenAire, it could either mean "CC-BY 1.0" or "4.0", its impossible
    // to say

    // Special case, as OpenAire returns "CC 0" and DAMAP has "CCZero"
    if ("CC 0".equalsIgnoreCase(normalized) || "CC0".equalsIgnoreCase(normalized)) {
      return ELicense.CCZERO;
    }
    ELicense returnLicense = ELicense.getLicense(normalized);
    // OpenAire returns licenses with whitespaces, like CC BY, but DAMAP enums are named like CCBY
    if (returnLicense == null) {
      returnLicense = ELicense.getLicense(normalized.replace(" ", ""));
    }
    return returnLicense;
  }

  /**
   * Maps SKG-IF dates to {@link Date} and returns the earliest
   *
   * @param values date strings to map and filter
   * @return earliest {@link Date} mapped from input date strings
   */
  private Date earliestDate(List<String> values) {
    if (values == null) return null;
    return values.stream()
        .map(OpenAireMapper::parseDate)
        .filter(Objects::nonNull)
        .min(Date::compareTo)
        .orElse(null);
  }

  /**
   * Takes SKG-IF manifestations of a dataset and finds the earliest start date. First checks
   * embargo dates and then publication dates, as {@link DatasetDO#getStartDate() startDate} is used
   * to signify embargos in datasets.
   *
   * @param manifestation that includes the access rights and dates to search
   * @return earliest {@link Date} mapped from embargo and publication dates
   */
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

  /**
   * Parses an ISO-8601 date string into a UTC {@link Date}.
   *
   * @param value date string in {@code yyyy-MM-dd} format
   * @return the parsed date, or {@code null} if the value is blank or invalid
   */
  private Date parseDate(String value) {
    if (value == null || value.isBlank()) return null;
    try {
      return Date.from(LocalDate.parse(value).atStartOfDay().toInstant(ZoneOffset.UTC));
    } catch (DateTimeParseException ignored) {
      return null;
    }
  }

  /**
   * Chooses a list from the input map by invoking {@link #preferredLocalizedValues(Map)}. Then
   * joins the values using a delimiter.
   *
   * @param values localized values to process
   * @param delimiter delimiter used to join the values
   * @return the joined values, or {@code null} if no values are available
   */
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

  /**
   * Returns the preferred localized values list, prioritizing English, then the language-neutral
   * values, and finally the first non-blank localized values.
   *
   * @param values localized values by language
   * @return the preferred localized values, or {@code null} if none are available
   */
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

  /**
   * Returns the preferred localized string label, prioritizing English, then the language-neutral
   * label, and finally the first non-blank label.
   *
   * @param values localized labels by language
   * @return the preferred label, or {@code null} if none is available
   */
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

  /**
   * Returns the first non-blank value from the list.
   *
   * @param values values to search
   * @return the first non-blank value, or {@code null} if none is available
   */
  private String firstNonBlank(List<String> values) {
    if (values == null) return null;
    return values.stream()
        .map(OpenAireMapper::trimToNull)
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
  }

  /**
   * Trims the value and returns {@code null} for blank values.
   *
   * @param value value to trim
   * @return the trimmed value, or {@code null} if blank or {@code null}
   */
  private String trimToNull(String value) {
    if (value == null || value.isBlank()) return null;
    return value.trim();
  }

  /**
   * Truncates the value to the specified maximum length.
   *
   * @param value value to truncate
   * @param maximumLength maximum allowed length
   * @return the truncated value, or the original value if it does not exceed the limit
   */
  private String truncate(String value, int maximumLength) {
    if (value == null || value.length() <= maximumLength) return value;
    return value.substring(0, maximumLength);
  }

  /**
   * Creates an {@link IdentifierDO} for the given DOI.
   *
   * @param doi DOI string
   * @return a DOI {@link IdentifierDO}
   */
  private IdentifierDO createDoiIdentifier(String doi) {
    IdentifierDO identifier = new IdentifierDO();
    identifier.setType(EIdentifierType.DOI);
    identifier.setIdentifier(doi);
    return identifier;
  }

  /**
   * Adds the data type to the types list if it is not already present.
   *
   * @param type data type to add
   * @param types list of data types
   */
  private void addType(EDataType type, List<EDataType> types) {
    if (!types.contains(type)) types.add(type);
  }
}
