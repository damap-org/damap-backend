package org.damap.base.rest;

import io.quarkus.security.Authenticated;
import io.quarkus.security.AuthenticationFailedException;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.Date;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.repo.ConsentRepo;
import org.damap.base.rest.administration.domain.ConsentDO;
import org.damap.base.rest.administration.service.ConsentService;
import org.damap.base.rest.auth.UserSyncService;
import org.damap.base.security.SecurityService;

/** ConsentResource class. */
@Path("/api/consent")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@JBossLog
public class ConsentResource {

  @Inject ConsentService consentService;

  @Inject SecurityService securityService;

  @Inject ConsentRepo consentRepo;

  @Inject UserSyncService userSyncService;

  /**
   * getConsent.
   *
   * @return a {@link org.damap.base.rest.administration.domain.ConsentDO} object
   */
  @GET
  public ConsentDO getConsent() {
    // TODO: this is a replacement for the DamapSecurityAugmentor, since it doesnt work with
    // multitenancy
    // since the security context isnt built when Hibernate asks for a tenant
    // as a workaround, users will be synced on this method, since its called everytime a user wants
    // to visit the tool
    userSyncService.syncUser(
        securityService.getUserId(),
        securityService.getEmail(),
        securityService.getUserName(),
        securityService.getFirstName(),
        securityService.getLastName());

    if (consentRepo.getConsentByUniversityId(this.getPersonId()) != null) {
      return consentService.getConsentByUser(this.getPersonId());
    } else {
      ConsentDO consent = new ConsentDO();
      String personId = this.getPersonId();
      consent.setUniversityId(personId);
      consent.setGivenDate(new Date());
      consent.setConsentGiven(false);
      return consentService.create(consent);
    }
  }

  /**
   * saveConsent.
   *
   * @param consentDO a {@link org.damap.base.rest.administration.domain.ConsentDO} object
   * @return a {@link org.damap.base.rest.administration.domain.ConsentDO} object
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public ConsentDO saveConsent(ConsentDO consentDO) {
    String personId = this.getPersonId();
    consentDO.setUniversityId(personId);
    consentDO.setGivenDate(new Date());
    if (consentService.getConsentByUser(this.getPersonId()) != null) {
      return consentService.update(consentDO);
    } else {
      return consentService.create(consentDO);
    }
  }

  private String getPersonId() {
    if (securityService == null) {
      throw new AuthenticationFailedException("User ID is missing.");
    }
    return securityService.getUserId();
  }
}
