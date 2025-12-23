package org.damap.base.rest;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.domain.Translation;
import org.damap.base.rest.translation.service.TranslationService;

/** TranslationResource class. */
@Path("/api/translations")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@JBossLog
public class TranslationResource {

  @Inject TranslationService translationService;

  /**
   * getTranslations.
   *
   * @param language a {@link java.lang.String} object
   * @return a {@link java.util.List} of Translation objects
   */
  @GET
  @Path("/{language}")
  public List<Translation> getTranslations(@PathParam("language") String language) {
    log.infov("GET /api/translations/{0}", language);
    return translationService.getActiveTranslations(language);
  }

  /**
   * createLanguage.
   *
   * @param newLanguage a {@link java.lang.String} object
   */
  @POST
  @Path("/language/{newLanguage}")
  @RolesAllowed("Damap Admin")
  public void createLanguage(@PathParam("newLanguage") String newLanguage) {
    log.infov("POST /api/translations/language/{0}", newLanguage);
    translationService.createLanguage(newLanguage);
  }

  /**
   * updateTranslation.
   *
   * @param translation a {@link org.damap.base.domain.Translation} object
   * @return the updated Translation object
   */
  @PATCH
  @RolesAllowed("Damap Admin")
  public Translation updateTranslation(@Valid Translation translation) {
    log.info("PATCH /api/translations");
    log.info(translation);
    return translationService.updateTranslation(translation);
  }

  /**
   * deleteLanguage.
   *
   * @param language a {@link java.lang.String} object
   */
  @DELETE
  @Path("/language/{language}")
  @RolesAllowed("Damap Admin")
  public void deleteLanguage(@PathParam("language") String language) {
    log.infov("DELETE /api/translations/language/{0}", language);
    translationService.deleteLanguage(language);
  }
}
