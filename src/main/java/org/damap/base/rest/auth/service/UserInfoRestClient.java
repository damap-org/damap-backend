package org.damap.base.rest.auth.service;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/userinfo")
@RegisterRestClient(configKey = "rest.auth")
public interface UserInfoRestClient {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  Map<String, Object> getUserInfo(@HeaderParam("Authorization") String bearerToken);
}
