package org.damap.base.rest.rda.service;

import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.ForbiddenException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;
import org.damap.base.rda.dmpcommonstandard.Booleanish;
import org.damap.base.rda.dmpcommonstandard.CommonStandardCompatibilityException;
import org.damap.base.rda.dmpcommonstandard.DMPDocument;
import org.damap.base.rda.dmpcommonstandard.DMPListResponseBody;
import org.damap.base.rda.dmpcommonstandard.DMPMapper;
import org.damap.base.rda.dmpcommonstandard.DMPWithID;
import org.damap.base.rda.dmpcommonstandard.RdaDmpSearchParams;
import org.damap.base.repo.DmpRepo;
import org.damap.base.rest.dmp.domain.DmpDO;
import org.damap.base.rest.dmp.service.DmpService;
import org.damap.base.security.SecurityService;
import org.damap.base.validation.AccessValidator;

@ApplicationScoped
public class RdaDmpService {

  private final DMPMapper dmpMapper = new DMPMapper(false);

  @Inject DmpService dmpService;

  @Inject DmpRepo dmpRepo;

  @Inject SecurityService securityService;

  @Inject AccessValidator accessValidator;

  public RdaDmpSearchParams createSearchParams(
      int offset,
      int count,
      List<String> sort,
      String createdBefore,
      String createdAfter,
      String modifiedBefore,
      String modifiedAfter,
      List<String> languages,
      List<String> contactIds,
      List<String> contributorIds,
      List<String> datasetIds,
      List<String> metadataStandardIds,
      List<String> dmpIds,
      List<String> funderIds,
      List<String> grantIds,
      String query,
      String ethicalIssuesExist,
      String embargoBefore,
      String embargoAfter) {
    return new RdaDmpSearchParams(
        offset,
        count,
        cleanList(sort),
        parseDateTime(createdBefore, "created_before"),
        parseDateTime(createdAfter, "created_after"),
        parseDateTime(modifiedBefore, "modified_before"),
        parseDateTime(modifiedAfter, "modified_after"),
        cleanList(languages),
        cleanList(contactIds),
        cleanList(contributorIds),
        cleanList(datasetIds),
        cleanList(metadataStandardIds),
        cleanList(dmpIds),
        cleanList(funderIds),
        cleanList(grantIds),
        cleanString(query),
        parseBooleanish(ethicalIssuesExist),
        cleanString(embargoBefore),
        cleanString(embargoAfter));
  }

  public DMPListResponseBody listDMPs(RdaDmpSearchParams params) {
    validatePagination(params.offset(), params.count());

    String personId = getPersonId();

    List<DmpDO> dmps = dmpService.getDmpDOListByPersonId(personId);

    // TODO: This should not be in the service layer, but in the database layer instead
    // When we get the new updated OpenAPi document, we should move all of the code to db queries
    List<DmpDO> filtered =
        dmps.stream()
            .filter(dmp -> matchesCreatedAfter(dmp, params.createdAfter()))
            .filter(dmp -> matchesCreatedBefore(dmp, params.createdBefore()))
            .filter(dmp -> matchesModifiedAfter(dmp, params.modifiedAfter()))
            .filter(dmp -> matchesModifiedBefore(dmp, params.modifiedBefore()))
            .filter(dmp -> matchesDmpIds(dmp, params.dmpIds()))
            .filter(dmp -> matchesQuery(dmp, params.query()))
            .filter(dmp -> matchesEthicalIssues(dmp, params.ethicalIssuesExist()))
            .filter(dmp -> matchesDatasetIds(dmp, params.datasetIds()))
            .filter(dmp -> matchesContactIds(dmp, params.contactIds()))
            .filter(dmp -> matchesContributorIds(dmp, params.contributorIds()))
            .filter(dmp -> matchesMetadataStandardIds(dmp, params.metadataStandardIds()))
            .filter(dmp -> matchesLanguages(dmp, params.languages()))
            .filter(dmp -> matchesFunderIds(dmp, params.funderIds()))
            .filter(dmp -> matchesGrantIds(dmp, params.grantIds()))
            .filter(dmp -> matchesEmbargoBefore(dmp, params.embargoBefore()))
            .filter(dmp -> matchesEmbargoAfter(dmp, params.embargoAfter()))
            .toList();

    // TODO: Take care of funder ID and grant ID?

    List<DmpDO> sorted = sortDmps(filtered, params.sort());

    List<DMPWithID> items =
        sorted.stream().skip(params.offset()).limit(params.count()).map(this::toRdaDmp).toList();

    return new DMPListResponseBody().totalCount(Math.toIntExact(filtered.size())).items(items);
  }

  public RdaDmpResult createDMP(DMPDocument rdaDmpDocument) {
    String personId = getPersonId();

    try {
      DMPWithID rdaInput = new DMPWithID().id("0").dmp(rdaDmpDocument.getDmp());

      DmpDO damapInput = dmpMapper.convert(rdaInput);
      damapInput.setId(null);
      damapInput.setCreated(null);
      damapInput.setModified(null);

      DmpDO createdDmp = dmpService.create(damapInput, personId);

      return new RdaDmpResult(toRdaDmp(createdDmp), lastModified(createdDmp));
    } catch (CommonStandardCompatibilityException
        | NullPointerException
        | IndexOutOfBoundsException
        | IllegalArgumentException e) {
      throw new BadRequestException("Invalid RDA DMP payload: " + e.getMessage(), e);
    }
  }

  public RdaDmpResult getDMP(String id) {
    String personId = getPersonId();
    long dmpId = parseId(id);

    ensureDmpExists(dmpId);

    if (!accessValidator.canViewDmp(dmpId, personId)) {
      throw new ForbiddenException(unauthorizedMessage(dmpId));
    }

    DmpDO dmp = dmpService.getDmpById(dmpId);

    return new RdaDmpResult(toRdaDmp(dmp), lastModified(dmp));
  }

  public DMPDocument getDMPDocument(long dmpId) {
    ensureDmpExists(dmpId);

    DmpDO dmp = dmpService.getDmpById(dmpId);

    return new DMPDocument().dmp(toRdaDmp(dmp).getDmp());
  }

  public RdaDmpResult updateDMP(String id, DMPDocument rdaDmpDocument, String ifUnmodifiedSince) {
    String personId = getPersonId();
    long dmpId = parseId(id);

    ensureDmpExists(dmpId);

    if (!accessValidator.canEditDmp(dmpId, personId)) {
      throw new ForbiddenException(unauthorizedMessage(dmpId));
    }

    DmpDO existingDmp = dmpService.getDmpById(dmpId);
    checkIfUnmodifiedSince(existingDmp, ifUnmodifiedSince);

    try {
      DMPWithID rdaInput = new DMPWithID().id(id).dmp(rdaDmpDocument.getDmp());

      DmpDO damapInput = dmpMapper.convert(rdaInput);
      damapInput.setId(dmpId);

      DmpDO updatedDmp = dmpService.update(damapInput);

      return new RdaDmpResult(toRdaDmp(updatedDmp), lastModified(updatedDmp));
    } catch (CommonStandardCompatibilityException
        | NullPointerException
        | IndexOutOfBoundsException
        | IllegalArgumentException e) {
      throw new BadRequestException("Invalid RDA DMP payload: " + e.getMessage(), e);
    }
  }

  public void deleteDMP(String id) {
    String personId = getPersonId();
    long dmpId = parseId(id);

    ensureDmpExists(dmpId);

    if (!accessValidator.canDeleteDmp(dmpId, personId)) {
      throw new ForbiddenException(unauthorizedMessage(dmpId));
    }

    dmpService.delete(dmpId);
  }

  @Transactional
  public DmpDO importRdaDmp(DMPDocument rdaDmpDocument) {
    String personId = getPersonId();

    validateImportDocument(rdaDmpDocument);

    try {
      DMPWithID rdaInput = new DMPWithID().id("0").dmp(rdaDmpDocument.getDmp());

      DmpDO damapInput = dmpMapper.convert(rdaInput);

      damapInput.setId(null);
      damapInput.setCreated(null);
      damapInput.setModified(null);

      return dmpService.create(damapInput, personId);
    } catch (CommonStandardCompatibilityException
        | NullPointerException
        | IndexOutOfBoundsException
        | IllegalArgumentException e) {
      throw new BadRequestException("Invalid RDA DMP import payload: " + e.getMessage(), e);
    }
  }

  private void validateImportDocument(DMPDocument document) {
    if (document == null || document.getDmp() == null) {
      throw new BadRequestException("Request body must contain a dmp object");
    }

    if (document.getDmp().getTitle().isBlank()) {
      throw new BadRequestException("DMP title is required");
    }

    if (document.getDmp().getDataset().isEmpty()) {
      throw new BadRequestException("At least one dataset is required");
    }
  }

  private void validatePagination(int offset, int count) {
    if (offset < 0) {
      throw new BadRequestException("offset must be greater than or equal to 0");
    }

    if (count < 1 || count > 100) {
      throw new BadRequestException("count must be between 1 and 100");
    }
  }

  private OffsetDateTime parseDateTime(String value, String fieldName) {
    if (value == null || value.isBlank()) {
      return null;
    }

    try {
      return OffsetDateTime.parse(value);
    } catch (Exception e) {
      throw new BadRequestException("Invalid " + fieldName + ": " + value, e);
    }
  }

  private Booleanish parseBooleanish(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    return switch (value.toLowerCase(Locale.ROOT)) {
      case "yes" -> Booleanish.YES;
      case "no" -> Booleanish.NO;
      case "unknown" -> Booleanish.UNKNOWN;
      default -> throw new BadRequestException("Invalid ethical_issues_exist: " + value);
    };
  }

  private List<String> cleanList(List<String> values) {
    if (values == null) {
      return List.of();
    }

    return values.stream()
        .filter(Objects::nonNull)
        .flatMap(value -> Stream.of(value.split(",")))
        .map(String::trim)
        .filter(value -> !value.isBlank())
        .toList();
  }

  private String cleanString(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    return value.trim();
  }

  private OffsetDateTime toOffsetDateTime(Date date) {
    if (date == null) {
      return null;
    }

    return date.toInstant().atOffset(ZoneOffset.UTC);
  }

  private boolean isOnOrAfter(Date date, OffsetDateTime boundary) {
    if (boundary == null) {
      return true;
    }

    OffsetDateTime value = toOffsetDateTime(date);
    return value != null && (value.isAfter(boundary) || value.isEqual(boundary));
  }

  private boolean isOnOrBefore(Date date, OffsetDateTime boundary) {
    if (boundary == null) {
      return true;
    }

    OffsetDateTime value = toOffsetDateTime(date);
    return value != null && (value.isBefore(boundary) || value.isEqual(boundary));
  }

  private boolean matchesCreatedAfter(DmpDO dmp, OffsetDateTime createdAfter) {
    return isOnOrAfter(dmp.getCreated(), createdAfter);
  }

  private boolean matchesCreatedBefore(DmpDO dmp, OffsetDateTime createdBefore) {
    return isOnOrBefore(dmp.getCreated(), createdBefore);
  }

  private boolean matchesModifiedAfter(DmpDO dmp, OffsetDateTime modifiedAfter) {
    return isOnOrAfter(dmp.getModified(), modifiedAfter);
  }

  private boolean matchesModifiedBefore(DmpDO dmp, OffsetDateTime modifiedBefore) {
    return isOnOrBefore(dmp.getModified(), modifiedBefore);
  }

  private boolean matchesDmpIds(DmpDO dmp, List<String> dmpIds) {
    // TODO: There is no DMP identifier in DAMAP like a DOI - therefore the search always fails
    // add DMP Identifier into the data model to at least use when importing maDMPs
    return false;
  }

  private boolean matchesQuery(DmpDO dmp, String query) {
    if (query == null || query.isBlank()) {
      return true;
    }

    String q = query.toLowerCase(Locale.ROOT);

    boolean dmpMatch =
        containsIgnoreCase(dmp.getTitle(), q)
            || containsIgnoreCase(dmp.getDescription(), q)
            || containsIgnoreCase(dmp.getMetadata(), q)
            || containsIgnoreCase(dmp.getDocumentation(), q)
            || containsIgnoreCase(dmp.getDataGeneration(), q)
            || containsIgnoreCase(dmp.getStructure(), q)
            || containsIgnoreCase(dmp.getTargetAudience(), q)
            || containsIgnoreCase(dmp.getTools(), q);

    if (dmpMatch) {
      return true;
    }

    if (dmp.getProject() != null
        && (containsIgnoreCase(dmp.getProject().getTitle(), q)
            || containsIgnoreCase(dmp.getProject().getDescription(), q))) {
      return true;
    }

    if (dmp.getDatasets() != null
        && dmp.getDatasets().stream()
            .anyMatch(
                dataset ->
                    containsIgnoreCase(dataset.getTitle(), q)
                        || containsIgnoreCase(dataset.getDescription(), q))) {
      return true;
    }

    return dmp.getContributors() != null
        && dmp.getContributors().stream()
            .anyMatch(
                contributor ->
                    containsIgnoreCase(contributor.getFirstName(), q)
                        || containsIgnoreCase(contributor.getLastName(), q)
                        || containsIgnoreCase(contributor.getMbox(), q)
                        || containsIgnoreCase(contributor.getUniversityId(), q));
  }

  private boolean containsIgnoreCase(String value, String queryLowercase) {
    return value != null && value.toLowerCase(Locale.ROOT).contains(queryLowercase);
  }

  private boolean matchesEthicalIssues(DmpDO dmp, Booleanish expected) {
    if (expected == null) {
      return true;
    }

    Boolean value = dmp.getEthicalIssuesExist();

    return switch (expected) {
      case YES -> Boolean.TRUE.equals(value);
      case NO -> Boolean.FALSE.equals(value);
      case UNKNOWN -> value == null;
    };
  }

  private boolean matchesDatasetIds(DmpDO dmp, List<String> datasetIds) {
    if (datasetIds == null || datasetIds.isEmpty()) {
      return true;
    }

    if (dmp.getDatasets() == null) {
      return false;
    }

    return dmp.getDatasets().stream()
        .anyMatch(
            dataset ->
                dataset.getDatasetId() != null
                    && datasetIds.stream()
                        .anyMatch(
                            id -> equalsIgnoreCase(id, dataset.getDatasetId().getIdentifier())));
  }

  private boolean matchesContactIds(DmpDO dmp, List<String> contactIds) {
    if (contactIds == null || contactIds.isEmpty()) {
      return true;
    }

    var contact = dmp.getContact();
    if (contact == null || contact.getPersonId() == null) {
      return false;
    }

    String identifier = contact.getPersonId().getIdentifier();

    return contactIds.stream().anyMatch(id -> equalsIgnoreCase(id, identifier));
  }

  private boolean matchesContributorIds(DmpDO dmp, List<String> contributorIds) {
    if (contributorIds == null || contributorIds.isEmpty()) {
      return true;
    }

    if (dmp.getContributors() == null) {
      return false;
    }

    return dmp.getContributors().stream()
        .anyMatch(
            contributor ->
                contributor.getPersonId() != null
                    && contributorIds.stream()
                        .anyMatch(
                            id -> equalsIgnoreCase(id, contributor.getPersonId().getIdentifier())));
  }

  private boolean matchesMetadataStandardIds(DmpDO dmp, List<String> metadataStandardIds) {
    if (metadataStandardIds == null || metadataStandardIds.isEmpty()) {
      return true;
    }

    return metadataStandardIds.stream()
        .anyMatch(id -> containsIgnoreCase(dmp.getMetadata(), id.toLowerCase(Locale.ROOT)));
  }

  private List<DmpDO> sortDmps(List<DmpDO> dmps, List<String> sort) {
    if (sort == null || sort.isEmpty()) {
      return dmps.stream()
          .sorted(
              Comparator.comparing(
                      DmpDO::getCreated, Comparator.nullsLast(Comparator.naturalOrder()))
                  .reversed())
          .toList();
    }

    Comparator<DmpDO> comparator = null;

    for (String sortField : sort) {
      Comparator<DmpDO> next = comparatorForSortField(sortField);

      if (comparator == null) {
        comparator = next;
      } else {
        comparator = comparator.thenComparing(next);
      }
    }

    return dmps.stream().sorted(comparator).toList();
  }

  private Comparator<DmpDO> comparatorForSortField(String rawSortField) {
    if (rawSortField == null || rawSortField.isBlank()) {
      throw new BadRequestException("sort[] contains an empty value");
    }

    boolean descending = rawSortField.startsWith("-");
    String sortField = descending ? rawSortField.substring(1) : rawSortField;

    Comparator<DmpDO> comparator =
        switch (sortField) {
          case "title" ->
              Comparator.comparing(
                  DmpDO::getTitle, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
          case "created" ->
              Comparator.comparing(
                  DmpDO::getCreated, Comparator.nullsLast(Comparator.naturalOrder()));
          case "modified" ->
              Comparator.comparing(
                  DmpDO::getModified, Comparator.nullsLast(Comparator.naturalOrder()));
          default -> throw new BadRequestException("Unsupported sort field: " + rawSortField);
        };

    return descending ? comparator.reversed() : comparator;
  }

  private boolean equalsIgnoreCase(String a, String b) {
    return a != null && b != null && a.equalsIgnoreCase(b);
  }

  private void ensureDmpExists(long dmpId) {
    if (dmpRepo.findById(dmpId) == null) {
      throw new NotFoundException("DMP with id " + dmpId + " not found");
    }
  }

  private DMPWithID toRdaDmp(DmpDO dmp) {
    try {
      return dmpMapper.convert(dmp);
    } catch (RuntimeException e) {
      throw new InternalServerErrorException(
          "Could not convert DAMAP DMP with id " + dmp.getId() + " to RDA format", e);
    }
  }

  private void checkIfUnmodifiedSince(DmpDO existingDmp, String ifUnmodifiedSince) {
    if (ifUnmodifiedSince == null || ifUnmodifiedSince.isBlank()) {
      return;
    }

    try {
      OffsetDateTime clientTimestamp =
          java.time.ZonedDateTime.parse(ifUnmodifiedSince, DateTimeFormatter.RFC_1123_DATE_TIME)
              .toOffsetDateTime()
              .withNano(0);

      Date serverDate =
          existingDmp.getModified() != null ? existingDmp.getModified() : existingDmp.getCreated();

      if (serverDate == null) {
        return;
      }

      OffsetDateTime serverTimestamp = serverDate.toInstant().atOffset(ZoneOffset.UTC).withNano(0);

      if (serverTimestamp.isAfter(clientTimestamp)) {
        throw new WebApplicationException(
            "DMP has been modified since the provided If-Unmodified-Since value",
            Response.Status.CONFLICT);
      }
    } catch (WebApplicationException e) {
      throw e;
    } catch (Exception e) {
      throw new BadRequestException("Invalid If-Unmodified-Since header", e);
    }
  }

  private String getPersonId() {
    if (securityService == null) {
      throw new AuthenticationFailedException("User ID is missing.");
    }
    return securityService.getUserId();
  }

  private long parseId(String id) {
    try {
      return Long.parseLong(id);
    } catch (NumberFormatException e) {
      throw new BadRequestException("Invalid DMP id: " + id, e);
    }
  }

  private String unauthorizedMessage(long id) {
    return "Not authorized to access dmp with id " + id;
  }

  private String lastModified(DmpDO dmp) {
    if (dmp != null && dmp.getModified() != null) {
      return DateTimeFormatter.RFC_1123_DATE_TIME.format(
          dmp.getModified().toInstant().atOffset(ZoneOffset.UTC));
    }

    if (dmp != null && dmp.getCreated() != null) {
      return DateTimeFormatter.RFC_1123_DATE_TIME.format(
          dmp.getCreated().toInstant().atOffset(ZoneOffset.UTC));
    }

    return DateTimeFormatter.RFC_1123_DATE_TIME.format(OffsetDateTime.now(ZoneOffset.UTC));
  }

  public record RdaDmpResult(DMPWithID body, String lastModified) {}

  private boolean matchesLanguages(DmpDO dmp, List<String> languages) {
    if (languages == null || languages.isEmpty()) {
      return true;
    }
    return languages.stream()
        .anyMatch(lang -> "eng".equalsIgnoreCase(lang) || "en".equalsIgnoreCase(lang));
  }

  private boolean matchesFunderIds(DmpDO dmp, List<String> funderIds) {
    if (funderIds == null || funderIds.isEmpty()) {
      return true;
    }
    if (dmp.getProject() == null || dmp.getProject().getFunding() == null) {
      return false;
    }
    var funderId = dmp.getProject().getFunding().getFunderId();
    if (funderId == null || funderId.getIdentifier() == null) {
      return false;
    }
    return funderIds.stream().anyMatch(id -> equalsIgnoreCase(id, funderId.getIdentifier()));
  }

  private boolean matchesGrantIds(DmpDO dmp, List<String> grantIds) {
    if (grantIds == null || grantIds.isEmpty()) {
      return true;
    }
    if (dmp.getProject() == null || dmp.getProject().getFunding() == null) {
      return false;
    }
    var grantId = dmp.getProject().getFunding().getGrantId();
    if (grantId == null || grantId.getIdentifier() == null) {
      return false;
    }
    return grantIds.stream().anyMatch(id -> equalsIgnoreCase(id, grantId.getIdentifier()));
  }

  private boolean matchesEmbargoBefore(DmpDO dmp, String embargoBeforeStr) {
    if (embargoBeforeStr == null || embargoBeforeStr.isBlank()) {
      return true;
    }
    OffsetDateTime boundary = parseDateTime(embargoBeforeStr, "embargo_before");
    if (dmp.getDatasets() == null) {
      return false;
    }
    return dmp.getDatasets().stream()
        .anyMatch(dataset -> isOnOrBefore(dataset.getStartDate(), boundary));
  }

  private boolean matchesEmbargoAfter(DmpDO dmp, String embargoAfterStr) {
    if (embargoAfterStr == null || embargoAfterStr.isBlank()) {
      return true;
    }
    OffsetDateTime boundary = parseDateTime(embargoAfterStr, "embargo_after");
    if (dmp.getDatasets() == null) {
      return false;
    }
    return dmp.getDatasets().stream()
        .anyMatch(dataset -> isOnOrAfter(dataset.getStartDate(), boundary));
  }
}
