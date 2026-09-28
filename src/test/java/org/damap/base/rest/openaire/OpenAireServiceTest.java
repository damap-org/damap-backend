package org.damap.base.rest.openaire;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.damap.base.enums.EErrorCode;
import org.damap.base.exception.DamapApiException;
import org.damap.base.rest.openaire.domain.OpenAireProduct;
import org.damap.base.rest.openaire.domain.OpenAireSearchResponse;
import org.damap.base.rest.openaire.service.OpenAireService;
import org.junit.jupiter.api.Test;

class OpenAireServiceTest {

  @Test
  void returnFirstMatchingResearchProduct() {
    OpenAireRemoteResource remoteResource = mock(OpenAireRemoteResource.class);
    OpenAireProduct product = new OpenAireProduct();
    OpenAireSearchResponse response = new OpenAireSearchResponse();
    response.setGraph(List.of(product));
    when(remoteResource.searchResearchProducts(
            "identifiers.id:10.5281/zenodo.4783814,identifiers.scheme:doi", 1))
        .thenReturn(response);
    OpenAireService service = new OpenAireService(remoteResource);

    OpenAireProduct result = service.searchResearchProduct("10.5281/zenodo.4783814");

    assertSame(product, result);
    verify(remoteResource)
        .searchResearchProducts("identifiers.id:10.5281/zenodo.4783814,identifiers.scheme:doi", 1);
  }

  @Test
  void throwNotFoundForEmptyResults() {
    OpenAireRemoteResource remoteResource = mock(OpenAireRemoteResource.class);
    when(remoteResource.searchResearchProducts(
            "identifiers.id:10.999999/missing,identifiers.scheme:doi", 1))
        .thenReturn(new OpenAireSearchResponse());
    OpenAireService service = new OpenAireService(remoteResource);

    DamapApiException exception =
        assertThrows(
            DamapApiException.class, () -> service.searchResearchProduct("10.999999/missing"));

    assertEquals(EErrorCode.OPENAIRE_NOT_FOUND, exception.getPayload().errorCode());
    assertEquals(jakarta.ws.rs.core.Response.Status.NOT_FOUND, exception.getStatus());
  }
}
