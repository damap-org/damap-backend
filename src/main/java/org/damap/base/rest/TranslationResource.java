package org.damap.base.rest;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.*;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.domain.Translation;
import org.damap.base.rest.translation.service.TranslationService;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

/** TranslationResource class. */
@Path("/api/languages")
@JBossLog
public class TranslationResource {

  @Inject TranslationService translationService;

  @POST
  public List<Translation> createLanguage(@RequestBody @Valid CreateLanguageRequest request) {
    return translationService.createLanguage(request.language());
  }

  @GET
  public List<String> getAllLanguages() {
    return translationService.getAllLanguages();
  }

  @GET
  @Path("/{language}")
  public List<Translation> getTranslationsForLanguage(
      @PathParam("language") @Valid @NotNull @Size(min = 2, max = 2) String language) {
    return translationService.getTranslationsForLanguage(language);
  }

  @PATCH
  @Path("/{language}")
  public List<Translation> activateLanguage(
      @PathParam("language") @Valid @NotNull @Size(min = 2, max = 2) String language,
      @RequestBody @Valid ActivateLanguageRequest request) {
    return translationService.activateLanguage(language, request.active());
  }

  @PATCH
  @Path("/{language}/translations/{key}")
  public Translation patchTranslationForLanguage(
      @PathParam("language") @Valid @NotNull @Size(min = 2, max = 2) String language,
      @PathParam("key") @Valid @NotNull @NotEmpty @Size(min = 1, max = 255) String key,
      @RequestBody @Valid PatchTranslationRequest request) {
    return translationService.patchTranslationForLanguage(language, key, request);
  }

  @DELETE
  @Path("/{language}")
  public void deleteLanguage(
      @PathParam("language") @Valid @NotNull @Size(min = 2, max = 2) String language) {
    translationService.deleteLanguage(language);
  }

  private record CreateLanguageRequest(@NotNull @Size(min = 2, max = 2) String language) {}

  private record ActivateLanguageRequest(@NotNull Boolean active) {}

  public record PatchTranslationRequest(@NotNull String value, @NotNull Boolean active) {}
}
