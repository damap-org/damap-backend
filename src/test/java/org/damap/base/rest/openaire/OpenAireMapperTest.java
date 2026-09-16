package org.damap.base.rest.openaire;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.damap.base.enums.EAccessRight;
import org.damap.base.enums.EDataAccessType;
import org.damap.base.enums.EDataSource;
import org.damap.base.enums.EDataType;
import org.damap.base.enums.EIdentifierType;
import org.damap.base.enums.ELicense;
import org.damap.base.rest.dmp.domain.DatasetDO;
import org.damap.base.rest.openaire.domain.OpenAireAccessRights;
import org.damap.base.rest.openaire.domain.OpenAireDates;
import org.damap.base.rest.openaire.domain.OpenAireManifestation;
import org.damap.base.rest.openaire.domain.OpenAireProduct;
import org.damap.base.rest.openaire.domain.OpenAireSearchResponse;
import org.damap.base.rest.openaire.mapper.OpenAireMapper;
import org.junit.jupiter.api.Test;

class OpenAireMapperTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void mapResearchProduct() throws Exception {
    OpenAireProduct product;
    try (InputStream fixture =
        getClass().getResourceAsStream("/json/openaireResearchProductsResponse.json")) {
      product = objectMapper.readValue(fixture, OpenAireSearchResponse.class).getGraph().get(0);
    }

    DatasetDO dataset = OpenAireMapper.map("10.5281/zenodo.4783814", product);

    assertEquals(
        "FIG. 12 in A survey of small mammals in the Volta Region of Ghana", dataset.getTitle());
    assertEquals(
        "Published as part of the related article. Cyclops Roundleaf Bat from Shiare.",
        dataset.getDescription());
    assertEquals("", dataset.getFileFormat());
    assertNull(dataset.getSize());
    assertEquals(EDataAccessType.OPEN, dataset.getDataAccess());
    assertEquals(
        Date.from(LocalDate.of(2021, 5, 20).atStartOfDay().toInstant(ZoneOffset.UTC)),
        dataset.getStartDate());
    assertEquals(List.of(EDataType.IMAGES), dataset.getType());
    assertEquals(ELicense.CCZERO, dataset.getLicense());
    assertEquals(EDataSource.REUSED, dataset.getSource());
    assertEquals(EIdentifierType.DOI, dataset.getDatasetId().getType());
    assertEquals("10.5281/zenodo.4783814", dataset.getDatasetId().getIdentifier());
    assertEquals(EAccessRight.READ, dataset.getSelectedProjectMembersAccess());
    assertEquals(EAccessRight.READ, dataset.getOtherProjectMembersAccess());
    assertEquals(EAccessRight.READ, dataset.getPublicAccess());
  }

  @Test
  void ignoreInvalidOptionalValuesAndTruncateTitle() {
    OpenAireProduct product = new OpenAireProduct();
    product.setTitles(java.util.Map.of("none", List.of("x".repeat(300))));
    product.setProductType("unknown");

    DatasetDO dataset = OpenAireMapper.map("10.9999/example", product);

    assertEquals(255, dataset.getTitle().length());
    assertNull(dataset.getStartDate());
    assertNull(dataset.getSize());
    assertNull(dataset.getDataAccess());
    assertEquals(List.of(EDataType.OTHER), dataset.getType());
  }

  @Test
  void preferEnglishLocalizedValuesWithoutCombiningLanguages() {
    OpenAireProduct product = new OpenAireProduct();
    Map<String, List<String>> titles = new LinkedHashMap<>();
    titles.put("de", List.of("Deutscher Titel"));
    titles.put("none", List.of("Title without language"));
    titles.put("en", List.of("English title"));
    product.setTitles(titles);
    product.setAbstracts(
        Map.of(
            "de", List.of("Deutsche Beschreibung"),
            "none", List.of("Description without language"),
            "en", List.of("First sentence.", "Second sentence.")));

    DatasetDO dataset = OpenAireMapper.map("10.9999/languages", product);

    assertEquals("English title", dataset.getTitle());
    assertEquals("First sentence. Second sentence.", dataset.getDescription());
  }

  @Test
  void preferValuesWithoutLanguageOverFirstAvailableLanguage() {
    OpenAireProduct product = new OpenAireProduct();
    Map<String, List<String>> titles = new LinkedHashMap<>();
    titles.put("de", List.of("Deutscher Titel"));
    titles.put("none", List.of("Title without language"));
    titles.put("en", List.of(" "));
    product.setTitles(titles);

    DatasetDO dataset = OpenAireMapper.map("10.9999/language-fallback", product);

    assertEquals("Title without language", dataset.getTitle());
  }

  @Test
  void mapSkgIfProductTypes() {
    assertEquals(EDataType.PLAIN_TEXT, mapProductType("literature"));
    assertEquals(EDataType.SOFTWARE_APPLICATIONS, mapProductType("research software"));
    assertEquals(EDataType.OTHER, mapProductType("research data"));
    assertEquals(EDataType.OTHER, mapProductType("other"));
  }

  @Test
  void mapUnavailableAccessToClosed() {
    OpenAireProduct product = productWithManifestation("unavailable", null, null);

    DatasetDO dataset = OpenAireMapper.map("10.9999/unavailable", product);

    assertEquals(EDataAccessType.CLOSED, dataset.getDataAccess());
  }

  @Test
  void preferEmbargoDateForEmbargoedManifestation() {
    OpenAireProduct product =
        productWithManifestation("embargoed", List.of("2025-06-01"), List.of("2024-01-15"));

    DatasetDO dataset = OpenAireMapper.map("10.9999/embargoed", product);

    assertEquals(
        Date.from(LocalDate.of(2025, 6, 1).atStartOfDay().toInstant(ZoneOffset.UTC)),
        dataset.getStartDate());
  }

  @Test
  void fallBackToPublicationDateWhenEmbargoDateIsInvalid() {
    OpenAireProduct product =
        productWithManifestation("embargo", List.of("invalid"), List.of("2024-01-15"));

    DatasetDO dataset = OpenAireMapper.map("10.9999/embargo-fallback", product);

    assertEquals(
        Date.from(LocalDate.of(2024, 1, 15).atStartOfDay().toInstant(ZoneOffset.UTC)),
        dataset.getStartDate());
  }

  @Test
  void mapCcZeroAliases() {
    OpenAireProduct cc0Product = productWithManifestation("open", null, null);
    cc0Product.getManifestations().get(0).setLicence("CC0");
    OpenAireProduct ccZeroProduct = productWithManifestation("open", null, null);
    ccZeroProduct.getManifestations().get(0).setLicence("CC 0");

    assertEquals(ELicense.CCZERO, OpenAireMapper.map("10.9999/cc0", cc0Product).getLicense());
    assertEquals(
        ELicense.CCZERO, OpenAireMapper.map("10.9999/cc-zero", ccZeroProduct).getLicense());
  }

  private EDataType mapProductType(String productType) {
    OpenAireProduct product = new OpenAireProduct();
    product.setProductType(productType);
    return OpenAireMapper.map("10.9999/product-type", product).getType().get(0);
  }

  private OpenAireProduct productWithManifestation(
      String accessStatus, List<String> embargoDates, List<String> publicationDates) {
    OpenAireDates dates = new OpenAireDates();
    if (embargoDates != null) dates.setEmbargo(embargoDates);
    if (publicationDates != null) dates.setPublication(publicationDates);

    OpenAireAccessRights accessRights = new OpenAireAccessRights();
    accessRights.setStatus(accessStatus);

    OpenAireManifestation manifestation = new OpenAireManifestation();
    manifestation.setAccessRights(accessRights);
    manifestation.setDates(dates);

    OpenAireProduct product = new OpenAireProduct();
    product.setManifestations(List.of(manifestation));
    return product;
  }
}
