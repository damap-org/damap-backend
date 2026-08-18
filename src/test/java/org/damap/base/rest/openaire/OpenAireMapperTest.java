package org.damap.base.rest.openaire;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import org.damap.base.enums.EAccessRight;
import org.damap.base.enums.EDataAccessType;
import org.damap.base.enums.EDataSource;
import org.damap.base.enums.EDataType;
import org.damap.base.enums.EIdentifierType;
import org.damap.base.enums.ELicense;
import org.damap.base.rest.dmp.domain.DatasetDO;
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
}
