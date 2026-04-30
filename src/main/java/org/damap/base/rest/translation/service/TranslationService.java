package org.damap.base.rest.translation.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.domain.Translation;
import org.damap.base.repo.TranslationRepo;
import org.damap.base.rest.TranslationResource.PatchTranslationRequest;
import org.damap.base.rest.translation.domain.LanguageSummary;

/** TranslationService class. */
@ApplicationScoped
@RequiredArgsConstructor
@JBossLog
public class TranslationService {

  private final TranslationRepo translationRepo;

  /**
   * createLanguage.
   *
   * <p>This method is idempotent. If the language already exists, it will return the existing
   * translations.
   *
   * @param newLanguage a {@link java.lang.String} language code
   * @return a {@link java.util.List} of Translation objects related to the new language code
   */
  @Transactional
  public List<Translation> createLanguage(String newLanguage) {
    List<Translation> existing = translationRepo.findByLanguage(newLanguage);
    if (!existing.isEmpty()) {
      return existing;
    }

    log.infov("Creating new language: {0}", newLanguage);

    List<Translation> englishTranslations = translationRepo.findByLanguage("en");

    for (Translation englishTranslation : englishTranslations) {
      Translation newTranslation =
          Translation.builder()
              .translationKey(englishTranslation.getTranslationKey())
              .language(newLanguage)
              .defaultValue(englishTranslation.getDefaultValue())
              .value(null)
              .active(true)
              .build();

      translationRepo.persist(newTranslation);
    }
    return translationRepo.findByLanguage(newLanguage);
  }

  /**
   * getAllLanguages.
   *
   * @return a {@link java.util.List} of language codes
   */
  public List<String> getAllLanguages() {
    log.infov("Getting all languages");
    return translationRepo.findAllLanguages();
  }

  /**
   * getActiveLanguages.
   *
   * @return a {@link java.util.List} of language codes
   */
  public List<String> getActiveLanguages() {
    log.infov("Getting active languages");
    return translationRepo.findActiveLanguages();
  }

  /**
   * getAllLanguageSummaries.
   *
   * @return a {@link java.util.List} of {@link LanguageSummary} for every language code, including
   *     whether the language is active
   */
  public List<LanguageSummary> getAllLanguageSummaries() {
    log.infov("Getting all language summaries");
    Set<String> activeLanguages = new HashSet<>(translationRepo.findActiveLanguages());
    return translationRepo.findAllLanguages().stream()
        .map(language -> new LanguageSummary(language, activeLanguages.contains(language)))
        .collect(Collectors.toList());
  }

  /**
   * activateLanguage.
   *
   * @param language a {@link java.lang.String} language code
   * @param active a {@link java.lang.Boolean} activation flag
   * @return a {@link java.util.List} of Translation objects related to the language code
   * @throws {@link jakarta.ws.rs.BadRequestException} if trying to deactivate the last active
   *     language
   */
  @Transactional
  public List<Translation> activateLanguage(String language, Boolean active) {
    List<Translation> translations = translationRepo.findByLanguage(language);
    if (translations.isEmpty()) {
      throw new NotFoundException("No translations found for language: " + language);
    }

    if (!active) {
      List<String> activeLanguages = translationRepo.findActiveLanguages();
      if (activeLanguages.size() <= 1 && activeLanguages.contains(language)) {
        throw new BadRequestException("Cannot deactivate the last active language");
      }
    }

    log.infov("Setting language active state: {0} -> {1}", language, active);

    for (Translation translation : translations) {
      translation.setActive(active);
    }

    return translations;
  }

  /**
   * getTranslationsForLanguage.
   *
   * @param language a {@link java.lang.String} language code
   * @return a {@link java.util.List} of Translation objects related to the language code
   */
  public List<Translation> getTranslationsForLanguage(String language) {
    log.infov("Getting translations for language: {0}", language);
    return translationRepo.findByLanguage(language);
  }

  /**
   * patchTranslationForLanguage.
   *
   * @param language a {@link java.lang.String} language code
   * @param key a {@link java.lang.String} key
   * @param request a {@link org.damap.base.rest.TranslationResource.PatchTranslationRequest} object
   *     containing the new value and active flag
   * @return the updated Translation object
   */
  @Transactional
  public Translation patchTranslationForLanguage(
      String language, String key, PatchTranslationRequest request) {
    Translation existing =
        translationRepo
            .findByKeyAndLanguage(key, language)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "Translation not found for language: " + language + " and key: " + key));

    log.infov("Updating translation {0} for language {1}", key, language);

    existing.setValue(request.value());
    existing.setActive(request.active());

    return existing;
  }

  /**
   * deleteLanguage.
   *
   * <p>WARNING: This is a destructive operation. All translations for the given language will be
   * permanently removed from the database.
   *
   * @param language a {@link java.lang.String} language code
   * @throws jakarta.ws.rs.BadRequestException if the language is "en"
   * @throws jakarta.ws.rs.NotFoundException if no translations exist for the given language
   */
  @Transactional
  public void deleteLanguage(String language) {
    // Prevent English language from deletion
    if ("en".equals(language)) {
      throw new BadRequestException("Cannot delete the English language");
    }

    if (translationRepo.deleteByLanguage(language) == 0) {
      throw new NotFoundException("No translations found for language: " + language);
    }
  }
}
