package org.damap.base.rest;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.domain.ColorTheme;
import org.damap.base.domain.Image;
import org.damap.base.domain.RecommendedRepository;
import org.damap.base.rest.admin.domain.BannerDO;
import org.damap.base.rest.admin.domain.RecommendedRepositoryDO;
import org.damap.base.rest.admin.service.AdminService;
import org.damap.base.rest.admin.service.RecommendedRepositoryService;
import org.damap.base.rest.theme.service.ColorThemeService;
import org.damap.base.rest.theme.service.ImageThemeService;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.multipart.FileUpload;

/** AdminResource class. */
@Path("/api/admin")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
@JBossLog
public class AdminResource {

  @Inject AdminService adminService;
  @Inject RecommendedRepositoryService recommendedRepositoryService;
  @Inject ColorThemeService colorThemeService;
  @Inject ImageThemeService imageThemeService;

  @GET
  @Path("/banner")
  public BannerDO getAppBanner() {
    log.info("Retrieving application banner");
    return this.adminService.getAppBanner();
  }

  @POST
  @Path("/banner")
  @RolesAllowed("${damap.auth.admin-role-name}")
  public BannerDO createAppBanner(@Valid BannerDO bannerDO) {
    log.info("Creating new application banner");
    log.info(bannerDO);
    return this.adminService.createAppBanner(bannerDO);
  }

  @PUT
  @Path("/banner")
  @RolesAllowed("${damap.auth.admin-role-name}")
  public BannerDO updateAppBanner(@Valid BannerDO bannerDO) {
    log.info("Updating application banner");
    log.info(bannerDO);
    return this.adminService.updateAppBanner(bannerDO);
  }

  @DELETE
  @Path("/banner")
  @RolesAllowed("${damap.auth.admin-role-name}")
  public void deleteAppBanner() {
    log.info("Deleting application banner");
    this.adminService.deleteAppBanner();
  }

  @GET
  @Path("/color-theme")
  @RolesAllowed("${damap.auth.admin-role-name}")
  public ColorTheme getColorTheme() {
    log.info("Retrieving color theme configuration");
    return this.colorThemeService.getTheme();
  }

  @PUT
  @Path("/color-theme")
  @RolesAllowed("${damap.auth.admin-role-name}")
  public ColorTheme uploadColorTheme(ColorTheme colorTheme) {
    log.info("Updating color theme configuration");
    return colorThemeService.uploadTheme(colorTheme);
  }

  @PUT
  @Path("/image-theme")
  @RolesAllowed("${damap.auth.admin-role-name}")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Image uploadImage(
      @RestForm("imageKey") String imageKey, @RestForm("file") FileUpload file) {
    log.info("Updating image with key: " + imageKey);
    return imageThemeService.uploadImage(imageKey, file);
  }

  @DELETE
  @Path("/image-theme")
  @RolesAllowed("${damap.auth.admin-role-name}")
  public void deleteImage(@QueryParam("imageKey") String imageKey) {
    log.info("Deleting image with key: " + imageKey);
    imageThemeService.deleteImage(imageKey);
  }

  @GET
  @Path("/recommended-repositories")
  @RolesAllowed("${damap.auth.admin-role-name}")
  public List<RecommendedRepositoryDO> getRecommendedRepositories() {
    log.info("Retrieving all recommended repositories");
    return this.recommendedRepositoryService.getRecommendedRepositoriesWithNames();
  }

  @POST
  @Path("/recommended-repositories")
  @RolesAllowed("${damap.auth.admin-role-name}")
  public RecommendedRepositoryDO createRecommendedRepository(
      @Valid RecommendedRepositoryDO recommendedRepositoryDO) {
    log.info("Creating new recommended repository: " + recommendedRepositoryDO.getRepositoryId());
    RecommendedRepository recommendedRepository = new RecommendedRepository();
    recommendedRepository.setRepositoryId(recommendedRepositoryDO.getRepositoryId());
    RecommendedRepository created =
        this.recommendedRepositoryService.createRecommendedRepository(recommendedRepository);
    recommendedRepositoryDO.setId(created.id);
    return recommendedRepositoryDO;
  }

  @DELETE
  @Path("/recommended-repositories/{id}")
  @RolesAllowed("${damap.auth.admin-role-name}")
  public void deleteRecommendedRepository(@RestPath Long id) {
    log.info("Deleting recommended repository with id: " + id);
    this.recommendedRepositoryService.deleteRecommendedRepository(id);
  }
}
