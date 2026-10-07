package org.damap.base.integration.generic;

import io.quarkus.rest.client.reactive.ClientExceptionMapper;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.jboss.resteasy.reactive.ResponseStatus;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
@RegisterClientHeaders(ClientHeaderFactory.class)
interface GenericCrisClient {
  @ClientExceptionMapper
  static RuntimeException toException(Response response) {
    if (response.getStatus() == 200) {
      return null;
    }
    // TODO: map structured exceptions
    return new RuntimeException("Lookup failed: " + response.getStatus());
  }

  @Path("/persons")
  @GET
  @ResponseStatus(200)
  SearchPersonsResponseBody searchPersons(
      @QueryParam("q") String query,
      @Min(0) @Max(10000) @DefaultValue("0") @QueryParam("offset") int offset,
      @Min(1) @Max(100) @DefaultValue("20") @QueryParam("count") int count);

  default List<DAMAPContact> getAllPersons(@QueryParam("q") String query) {
    var result = new ArrayList<DAMAPContact>();
    var offset = 0;
    while (true) {
      var queryResult = searchPersons(query, offset, 100);
      var items = queryResult.getItems();
      if (items == null || items.isEmpty()) {
        return result;
      }
      offset += items.size();
      result.addAll(items);
      var totalItems = queryResult.getTotalItems();
      if (totalItems != null && offset >= totalItems) {
        return result;
      }
    }
  }

  @Path("/persons/{systemPersonID}")
  @GET
  @ResponseStatus(200)
  DAMAPContact getPerson(@NotNull @PathParam("systemPersonID") String systemPersonID);

  @Path("/projects")
  @GET
  @ResponseStatus(200)
  SearchProjectsResponseBody searchProjects(
      @QueryParam("q") String query,
      @Min(0) @Max(10000) @DefaultValue("0") @QueryParam("offset") int offset,
      @Min(1) @Max(100) @DefaultValue("20") @QueryParam("count") int count);

  default List<DAMAPProject> getAllProjects(@QueryParam("q") String query) {
    var result = new ArrayList<DAMAPProject>();
    var offset = 0;
    while (true) {
      var queryResult = searchProjects(query, offset, 100);
      var items = queryResult.getItems();
      if (items == null || items.isEmpty()) {
        return result;
      }
      offset += items.size();
      result.addAll(items);
      var totalItems = queryResult.getTotalItems();
      if (totalItems != null && offset >= totalItems) {
        return result;
      }
    }
  }

  @Path("/projects/{systemProjectID}")
  @GET
  @ResponseStatus(200)
  DAMAPProject getProject(@NotNull @PathParam("systemProjectID") String systemProjectID);

  @Path("/projects/{systemProjectID}/staff")
  @GET
  @ResponseStatus(200)
  GetProjectStaffResponseBody getProjectStaff(
      @NotNull @PathParam("systemProjectID") String systemProjectID);

  @Path("/projects/{systemProjectID}/supplements")
  @GET
  @ResponseStatus(200)
  ProjectSupplement getProjectSupplements(
      @NotNull @PathParam("systemProjectID") String systemProjectID);

  @Path("/projects/{systemProjectID}/leader")
  @GET
  @ResponseStatus(200)
  DAMAPContributor getProjectLeader(@NotNull @PathParam("systemProjectID") String systemProjectID);

  @Path("/projects/recommended")
  @GET
  @ResponseStatus(200)
  GetRecommendedProjectsResponseBody getRecommendedProjects(
      @QueryParam("email") String email, @QueryParam("name") String name);
}
