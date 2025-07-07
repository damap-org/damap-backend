package org.damap.base.rest;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.rest.base.ResultList;
import org.damap.base.rest.base.Search;
import org.damap.base.rest.base.resource.ResourceSearch;
import org.damap.base.rest.dmp.domain.ContributorDO;
import org.damap.base.rest.dmp.domain.ProjectDO;
import org.damap.base.rest.dmp.service.DmpService;

/** ProjectResource class. */
@Path("/api/projects")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@JBossLog
public class ProjectResource implements ResourceSearch<ProjectDO> {

  @Inject ProjectService projectService;

  @Inject DmpService dmpService;

  /* TODO: Strategy for permission check required for restricted projects */
  /**
   * Returns a list of all project members.
   *
   * @param projectId a {@link java.lang.String} object
   * @return a {@link java.util.List} object
   */
  @GET
  @Path("/{id}/staff")
  public List<ContributorDO> getProjectMembers(@PathParam("id") String projectId) {
    log.info(String.format("Get Project Staff for Project ID=%s", projectId));
    return projectService.getProjectStaff(projectId);
  }

  /** {@inheritDoc} */
  @Override
  public ResultList<ProjectDO> search(UriInfo uriInfo) {
    var queryParams = uriInfo.getQueryParameters();
    log.info("Return projects for query=" + queryParams.toString());

    var resultList = projectService.search(Search.fromMap(queryParams));
    resultList.setItems(dmpService.checkExistingDmps(resultList.getItems()));

    return resultList;
  }

  /**
   * Returns a list of recommended project members based on the query from the frontend. See also
   * {@link Search}.
   *
   * @param uriInfo a {@link jakarta.ws.rs.core.UriInfo} object
   * @return a {@link org.damap.base.rest.base.ResultList} object
   */
  @GET
  @Path("/recommended")
  public ResultList<ProjectDO> recommended(@Context UriInfo uriInfo) {
    var queryParams = uriInfo.getQueryParameters();
    log.info("Return recommended projects for query=" + queryParams.toString());

    var resultList = projectService.getRecommended(Search.fromMap(queryParams));
    resultList.setItems(dmpService.checkExistingDmps(resultList.getItems()));

    return resultList;
  }
}
