package org.damap.base.rest;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.rest.dmp.domain.DatasetDO;
import org.damap.base.rest.openaire.mapper.OpenAireMapper;
import org.damap.base.rest.openaire.service.OpenAireService;
import org.jboss.resteasy.reactive.RestQuery;

/** OpenAireResource class. */
@Path("/api/openaire")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@JBossLog
public class OpenAireResource {

  @Inject OpenAireService openAireService;

  /**
   * search.
   *
   * @param doi a {@link java.lang.String} object
   * @return a {@link org.damap.base.rest.dmp.domain.DatasetDO} object
   */
  @GET
  public DatasetDO search(@RestQuery String doi) {
    log.info("Search for dataset with DOI: " + doi);
    return OpenAireMapper.map(doi, openAireService.searchResearchProduct(doi));
  }
}
