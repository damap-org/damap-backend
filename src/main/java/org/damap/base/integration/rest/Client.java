package org.damap.base.integration.rest;

import io.quarkus.rest.client.reactive.ClientExceptionMapper;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.ResponseStatus;

@RegisterRestClient(configKey = "rest.generic-cris")
@Produces(MediaType.APPLICATION_JSON)
@ClientHeaderParam(name = "Authorization", value = "Bearer ${apikey}")
@ClientHeaderParam(name = "accept", value = MediaType.APPLICATION_JSON)
interface Client {
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
  ListPersonsResponseBody listPersons(
      @QueryParam("q") String query,
      @Min(0) @Max(10000) @DefaultValue("0") @QueryParam("offset") int offset,
      @Min(1) @Max(100) @DefaultValue("20") @QueryParam("count") int count);

  default List<DAMAPContact> listAllPersons(@QueryParam("q") String query) {
    var result = new ArrayList<DAMAPContact>();
    var offset = 0;
    while (true) {
      var queryResult = listPersons(query, offset, 100);
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
  ListProjectsResponseBody listProjects(
      @QueryParam("q") String query,
      @Min(0) @Max(10000) @DefaultValue("0") @QueryParam("offset") int offset,
      @Min(1) @Max(100) @DefaultValue("20") @QueryParam("count") int count);

  default List<DAMAPProject> listAllProjects(@QueryParam("q") String query) {
    var result = new ArrayList<DAMAPProject>();
    var offset = 0;
    while (true) {
      var queryResult = listProjects(query, offset, 100);
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
  DAMAPProject getProject(@PathParam("systemProjectID") String systemProjectID);

  @Path("/projects/{systemProjectID}/contributors")
  @GET
  @ResponseStatus(200)
  ListProjectContributorsResponseBody listProjectContributors(
      @NotNull @PathParam("systemProjectID") String systemProjectID,
      @QueryParam("q") String query,
      @Min(0) @Max(10000) @DefaultValue("0") @QueryParam("offset") int offset,
      @Min(1) @Max(100) @DefaultValue("20") @QueryParam("count") int count);

  default List<DAMAPContributor> listAllProjectContributors(
      @NotNull String systemProjectID, @QueryParam("q") String query) {
    var result = new ArrayList<DAMAPContributor>();
    var offset = 0;
    while (true) {
      var queryResult = listProjectContributors(systemProjectID, query, offset, 100);
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
}
