package org.damap.base.rest.openaire;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import org.damap.base.rest.openaire.domain.OpenAireProduct;
import org.damap.base.rest.openaire.domain.OpenAireSearchResponse;
import org.junit.jupiter.api.Test;

class OpenAireSearchResponseTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void deserializeResearchProductsResponse() throws Exception {
    try (InputStream fixture =
        getClass().getResourceAsStream("/json/openaireResearchProductsResponse.json")) {
      assertNotNull(fixture);

      OpenAireSearchResponse response =
          objectMapper.readValue(fixture, OpenAireSearchResponse.class);

      assertEquals(1, response.getGraph().size());

      OpenAireProduct product = response.getGraph().get(0);
      assertEquals("research data", product.getProductType());
      assertEquals(
          "FIG. 12 in A survey of small mammals in the Volta Region of Ghana",
          product.getTitles().get("none").get(0));
      assertEquals(2, product.getAbstracts().get("none").size());
      assertEquals(
          "2021-05-20", product.getManifestations().get(0).getDates().getPublication().get(0));
      assertEquals("open", product.getManifestations().get(0).getAccessRights().getStatus());
      assertEquals("Image", product.getManifestations().get(0).getType().getLabels().get("en"));
      assertEquals("CC0", product.getManifestations().get(0).getLicence());
    }
  }
}
