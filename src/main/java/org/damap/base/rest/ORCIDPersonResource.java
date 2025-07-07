package org.damap.base.rest;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.enums.EIdentifierType;
import org.damap.base.integration.orcid.ORCIDPersonServiceImpl;
import org.damap.base.rest.dmp.domain.ContributorDO;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

/** PersonResource class. */
@Path("/api/orcid")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@JBossLog
public class ORCIDPersonResource {

  @Inject ORCIDPersonServiceImpl orcidPersonService;

  @POST
  @Path("/affiliation")
  public ContributorDO updateOrcidAffiliation(@RequestBody ContributorDO contributorDO) {

    if (contributorDO.getPersonId().getType() != EIdentifierType.ORCID) {
      throw new IllegalArgumentException(
          "Cannot correct affiliations for contributor with identifier type: "
              + contributorDO.getPersonId().getType());
    }

    return orcidPersonService.read(contributorDO.getPersonId().getIdentifier());
  }
}
