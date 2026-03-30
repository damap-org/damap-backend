package org.damap.base.rest;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.domain.ColorTheme;
import org.damap.base.domain.ExportTemplate;
import org.damap.base.domain.Image;
import org.damap.base.rest.admin.mapper.ExportTemplateDOMapper;
import org.damap.base.rest.config.domain.ConfigDO;
import org.damap.base.rest.config.domain.PersonServiceConfigurations;
import org.damap.base.rest.theme.service.ColorThemeService;
import org.damap.base.rest.theme.service.ImageThemeService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/** ConfigResource class. */
@Path("/api/config")
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
@JBossLog
public class ConfigResource {
  @ConfigProperty(name = "quarkus.oidc.token.issuer")
  String issuer;

  @ConfigProperty(name = "quarkus.oidc.client-id")
  String clientID;

  @ConfigProperty(name = "damap.auth.scope")
  String scope;

  @ConfigProperty(name = "damap.auth.user-roles-claim-path")
  String userRolesClaimPath;

  @ConfigProperty(name = "damap.auth.user-id-claim")
  String userIdClaim;

  @ConfigProperty(name = "damap.auth.name-claim")
  String nameClaim;

  @ConfigProperty(name = "damap.auth.given-name-claim")
  String givenNameClaim;

  @ConfigProperty(name = "damap.auth.family-name-claim")
  String familyNameClaim;

  @ConfigProperty(name = "damap.auth.email-claim")
  String emailClaim;

  @ConfigProperty(name = "damap.auth.admin-role-name")
  String adminRoleName;

  @ConfigProperty(name = "damap.env")
  String env;

  @ConfigProperty(name = "damap.person-services")
  PersonServiceConfigurations personServiceConfigurations;

  @ConfigProperty(name = "rest.fits/mp-rest/url")
  Optional<URL> fitsUrl;

  @ConfigProperty(name = "rest.gotenberg/mp-rest/url")
  Optional<URL> gotenbergUrl;

  @ConfigProperty(name = "damap.fields.ethical-report-enabled")
  boolean ethicalReportEnabled;

  @ConfigProperty(name = "damap.title", defaultValue = "DAMAP Tool")
  String appTitle;

  @Inject ColorThemeService colorThemeService;

  @Inject ImageThemeService imageThemeService;

  @Inject ExportTemplateService exportTemplateService;

  /**
   * config.
   *
   * @return a {@link org.damap.base.rest.config.domain.ConfigDO} object
   */
  @GET
  public ConfigDO config() {
    ConfigDO configDO = new ConfigDO();
    configDO.setIssuer(issuer);
    configDO.setClientID(clientID);
    configDO.setScope(scope);
    configDO.setUserRolesClaimPath(userRolesClaimPath);
    configDO.setUserIdClaim(userIdClaim);
    configDO.setNameClaim(nameClaim);
    configDO.setGivenNameClaim(givenNameClaim);
    configDO.setFamilyNameClaim(familyNameClaim);
    configDO.setEmailClaim(emailClaim);
    configDO.setAdminRoleName(adminRoleName);
    configDO.setResponseType("code"); // hardcoded since DAMAP only supports this flow
    configDO.setEnv(env);
    configDO.setAppTitle(appTitle);
    configDO.setPersonSearchServiceConfigs(personServiceConfigurations.getConfigs());
    configDO.setFitsServiceAvailable(getFitsServiceAvailability());
    configDO.setLivePreviewAvailable(getGotenbergServiceAvailability());
    configDO.setEthicalReportEnabled(ethicalReportEnabled);

    ColorTheme colorTheme = colorThemeService.getTheme();
    configDO.setColorTheme(colorTheme);

    List<Image> images = imageThemeService.getImages();
    configDO.setImages(images);

    List<ExportTemplate> templateEntities = ExportTemplate.listAll();
    configDO.setTemplates(ExportTemplateDOMapper.mapEntityListToDOList(templateEntities));

    return configDO;
  }

  private boolean getFitsServiceAvailability() {
    return fitsUrl.isPresent();
  }

  private boolean getGotenbergServiceAvailability() {
    return gotenbergUrl.isPresent();
  }
}
