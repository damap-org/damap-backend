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
import org.damap.base.rest.config.domain.*;
import org.damap.base.rest.config.domain.ConfigDO;
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

  @ConfigProperty(name = "rest.gotenberg/mp-rest/url")
  Optional<URL> gotenbergUrl;

  @Inject ColorThemeService colorThemeService;

  @Inject ImageThemeService imageThemeService;

  @Inject TenantConfigResolver tenantConfigResolver;

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
    DamapTenantAwareConfig tenantAwareConfig = tenantConfigResolver.getTenantAwareConfig();
    configDO.setAppTitle(tenantAwareConfig.title());
    // The ServiceConfig runtime interface proxy cannot be marshalled and sent to the frontend, so
    // we need a DO
    List<ServiceConfigDO> serviceConfigDOS =
        tenantAwareConfig.personServices().stream().map(ServiceConfigDO::new).toList();
    configDO.setPersonSearchServiceConfigs(serviceConfigDOS);
    configDO.setProjectSearchServiceConfig(tenantAwareConfig.projectService());
    configDO.setLivePreviewAvailable(getGotenbergServiceAvailability());
    configDO.setEthicalReportEnabled(tenantAwareConfig.fields().ethicalReportEnabled());

    ColorTheme colorTheme = colorThemeService.getTheme();
    configDO.setColorTheme(colorTheme);

    List<Image> images = imageThemeService.getImages();
    configDO.setImages(images);

    List<ExportTemplate> templateEntities = ExportTemplate.listAll();
    configDO.setTemplates(ExportTemplateDOMapper.mapEntityListToDOList(templateEntities));

    configDO.setMultitenancyEnabled(!tenantConfigResolver.isMultitenancyDisabled());
    return configDO;
  }

  private boolean getGotenbergServiceAvailability() {
    return gotenbergUrl.isPresent();
  }
}
