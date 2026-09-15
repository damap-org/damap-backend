package org.damap.base.rest.openaire.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.enums.EErrorCode;
import org.damap.base.exception.DamapApiException;
import org.damap.base.exception.ErrorDto;
import org.damap.base.rest.openaire.OpenAireRemoteResource;
import org.damap.base.rest.openaire.domain.OpenAireProduct;
import org.damap.base.rest.openaire.domain.OpenAireSearchResponse;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/** OpenAireService class. */
@ApplicationScoped
@JBossLog
public class OpenAireService {

  private static final int SINGLE_RESULT = 1;
  private static final String DOI_FILTER = "identifiers.id:%s,identifiers.scheme:doi";

  private final OpenAireRemoteResource openAireRemoteResource;

  @Inject
  public OpenAireService(@RestClient OpenAireRemoteResource openAireRemoteResource) {
    this.openAireRemoteResource = openAireRemoteResource;
  }

  /**
   * search.
   *
   * @param doi a {@link java.lang.String} object
   * @return the matching SKG-IF product
   */
  public OpenAireProduct searchResearchProduct(String doi) {
    OpenAireSearchResponse response =
        openAireRemoteResource.searchResearchProducts(DOI_FILTER.formatted(doi), SINGLE_RESULT);

    if (response == null || response.getGraph() == null || response.getGraph().isEmpty()) {
      throw new DamapApiException(
          new ErrorDto(
              EErrorCode.OPENAIRE_NOT_FOUND, "A research product couldn't be found with OpenAire"),
          jakarta.ws.rs.core.Response.Status.NOT_FOUND);
    }

    return response.getGraph().get(0);
  }
}
