package org.damap.base.rest;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.rest.admin.domain.BannerDO;
import org.damap.base.rest.admin.service.AdminService;

/** AdminResource class. */
@Path("/api/admin")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
@JBossLog
public class AdminResource {

  @Inject AdminService adminService;

  @GET
  @Path("/banner")
  public BannerDO getAppBanner() {
    log.info("GET /admin/banner");
    return this.adminService.getAppBanner();
  }

  @POST
  @Path("/banner")
  @RolesAllowed("${damap.auth.admin-role-name}")
  public BannerDO createAppBanner(@Valid BannerDO bannerDO) {
    log.info("POST /admin/banner");
    log.info(bannerDO);
    return this.adminService.createAppBanner(bannerDO);
  }

  @PUT
  @Path("/banner")
  @RolesAllowed("${damap.auth.admin-role-name}")
  public BannerDO updateAppBanner(@Valid BannerDO bannerDO) {
    log.info("PUT /admin/banner");
    log.info(bannerDO);
    return this.adminService.updateAppBanner(bannerDO);
  }

  @DELETE
  @Path("/banner")
  @RolesAllowed("${damap.auth.admin-role-name}")
  public void deleteAppBanner() {
    log.info("DELETE /admin/banner");
    this.adminService.deleteAppBanner();
  }
}
