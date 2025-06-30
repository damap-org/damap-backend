package org.damap.base.integration.mock;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.damap.base.rest.dmp.domain.ProjectDO;
import org.damap.base.rest.dmp.domain.ProjectSupplementDO;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/** MockProjectRestService interface. */
@Path("")
@RegisterRestClient(configKey = "rest.mock-projects")
@Produces(MediaType.APPLICATION_JSON)
interface MockProjectRestService {

  /**
   * getProjectDetails.
   *
   * @param uniId a {@link java.lang.String} object
   * @return a {@link java.util.List} object
   */
  @GET
  @Path("/projects")
  List<ProjectDO> getProjectDetails(@QueryParam("universityId") String uniId);

  /**
   * getProjectList.
   *
   * @param query a {@link java.lang.String} object
   * @return a {@link java.util.List} object
   */
  @GET
  @Path("/projects")
  List<ProjectDO> getProjectList(@QueryParam("q") String query);

  /**
   * getRecommended.
   *
   * @param description a {@link java.lang.String} object
   * @return a {@link java.util.List} object
   */
  @GET
  @Path("/projects")
  List<ProjectDO> getRecommended(
      @QueryParam("description_like") @DefaultValue("recommend") String description);

  /**
   * getProjectSupplement.
   *
   * @return a {@link ProjectSupplementDO} object
   */
  @GET
  @Path("/project-supplement")
  ProjectSupplementDO getProjectSupplement();
}
