package org.damap.base.rest;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.net.URL;
import java.util.Optional;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.rest.config.domain.ConfigDO;
import org.damap.base.rest.config.domain.PersonServiceConfigurations;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/** ConfigResource class. */
@Path("/api/config")
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
@JBossLog
public class ConfigResource {
  @ConfigProperty(name = "damap.auth.frontend.url")
  String authUrl;

  @ConfigProperty(name = "damap.auth.frontend.client")
  String authClient;

  @ConfigProperty(name = "damap.auth.scope")
  String authScope;

  @ConfigProperty(name = "damap.auth.user")
  String authUser;

  @ConfigProperty(name = "damap.env")
  String env;

  @ConfigProperty(name = "damap.person-services")
  PersonServiceConfigurations personServiceConfigurations;

  @ConfigProperty(name = "damap.fits-url")
  Optional<URL> fitsUrl;

  @ConfigProperty(name = "damap.gotenberg-url")
  Optional<URL> gotenbergUrl;

  @ConfigProperty(name = "damap.title", defaultValue = "DAMAP Tool")
  String appTitle;

  /**
   * config.
   *
   * @return a {@link org.damap.base.rest.config.domain.ConfigDO} object
   */
  @GET
  public ConfigDO config() {
    ConfigDO configDO = new ConfigDO();
    configDO.setAuthUrl(authUrl);
    configDO.setAuthClient(authClient);
    configDO.setAuthScope(authScope);
    configDO.setAuthUser(authUser);
    configDO.setEnv(env);
    configDO.setAppTitle(appTitle);
    configDO.setPersonSearchServiceConfigs(personServiceConfigurations.getConfigs());
    configDO.setFitsServiceAvailable(getFitsServiceAvailability());
    configDO.setLivePreviewAvailable(getGotenbergServiceAvailability());

    return configDO;
  }

  private boolean getFitsServiceAvailability() {
    return fitsUrl.isPresent();
  }

  private boolean getGotenbergServiceAvailability() {
    return gotenbergUrl.isPresent();
  }
}
