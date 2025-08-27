package org.damap.base.integration.pure;

import io.quarkus.arc.lookup.LookupIfProperty;
import jakarta.enterprise.inject.Typed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("")
@RegisterRestClient(configKey = "elsevier-pure")
@RegisterClientHeaders(PureAuthenticationHeaderFactory.class)
@Produces(MediaType.APPLICATION_JSON)
@Typed(HTTPBasedPureAPI.class)
@LookupIfProperty(name = "damap.elsevier-pure-backend", stringValue = "http")
interface HTTPBasedPureAPI extends PureAPI {
  /**
   * List all projects using pagination.
   *
   * @param size the size of the page.
   * @param offset the offset to start at.
   * @return the response containing the projects on that page.
   */
  @GET
  @Path("/projects")
  @Override
  PureAPIPaginatedProjectsResponse listAllProjects(
      @QueryParam("size") Long size, @QueryParam("offset") Long offset);

  /**
   * Retrieve a project with a specific ID.
   *
   * @param uuid the ID of the project.
   * @return the project if found, or null if the project was not found.
   */
  @GET
  @Path("/projects/{uuid}")
  @Override
  PureAPIProject getProject(@PathParam("uuid") String uuid);

  /**
   * Retrieve all persons in the database, paginated.
   *
   * @param size the size of the page.
   * @param offset the offset to start at.
   * @return the response containing the list of persons.
   */
  @GET
  @Path("/persons")
  @Override
  PureAPIPaginatedPersonsResponse listAllPersons(
      @QueryParam("size") Long size, @QueryParam("offset") Long offset);

  /**
   * Fetch a single person based on their ID.
   *
   * @param uuid the ID of the person to fetch.
   * @return the person if found, or null if the person was not found.
   */
  @GET
  @Path("/persons/{uuid}")
  @Override
  PureAPIPerson getPerson(@PathParam("uuid") String uuid);
}
