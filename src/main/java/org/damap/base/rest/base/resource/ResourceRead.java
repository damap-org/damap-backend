package org.damap.base.rest.base.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;

/** ResourceRead interface. */
public interface ResourceRead<E> {

  /**
   * Reads an object with the specified ID and
   *
   * @param id a {@link java.lang.String} object
   * @param uriInfo a {@link jakarta.ws.rs.core.UriInfo} object
   * @return a E object
   */
  @GET
  @Path("/{id}")
  E read(@PathParam("id") String id, @Context UriInfo uriInfo);
}
