package org.damap.base.rest.translation.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import java.util.List;
import java.util.Optional;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.domain.Translation;
import org.damap.base.repo.TranslationRepo;

/** TranslationService class. */
@ApplicationScoped
@JBossLog
public class TranslationService {

  private final TranslationRepo translationRepo;

  public TranslationService(TranslationRepo translationRepo) {
    this.translationRepo = translationRepo;
  }

  /**
   * getActiveTranslations.
   *
   * @param language a {@link java.lang.String} object
   * @return a {@link java.util.List} of Translation objects
   */
  public List<Translation> getActiveTranslations(String language) {
    log.infov("Getting active translations for language: {0}", language);
    return translationRepo.findByLanguageAndActive(language, true);
  }

  /**
   * createLanguage.
   *
   * @param newLanguage a {@link java.lang.String} object
   */
  @Transactional
  public void createLanguage(String newLanguage) {
    log.infov("Creating new language: {0}", newLanguage);

    List<Translation> existing = translationRepo.findByLanguageAndActive(newLanguage, true);
    if (!existing.isEmpty()) {
      throw new BadRequestException("Language already exists: " + newLanguage);
    }

    List<Translation> englishTranslations = translationRepo.findByLanguageAndActive("en", true);

    if (englishTranslations.isEmpty()) {
      throw new IllegalStateException("No English translations found to copy from");
    }

    for (Translation englishTranslation : englishTranslations) {
      Translation newTranslation = new Translation();
      newTranslation.setKey(englishTranslation.getKey());
      newTranslation.setLanguage(newLanguage);
      newTranslation.setDefaultValue(englishTranslation.getDefaultValue());
      newTranslation.setValue(null);
      newTranslation.setActive(true);

      translationRepo.persist(newTranslation);
    }

    log.infov("Created {0} translations for language {1}", englishTranslations.size(), newLanguage);
  }

  /**
   * updateTranslation.
   *
   * @param translation a {@link org.damap.base.domain.Translation} object
   */
  @Transactional
  public Translation updateTranslation(Translation translation) {
    log.infov(
        "Updating translation: id={0}, key={1}, language={2}",
        translation.id, translation.getKey(), translation.getLanguage());

    Optional<Translation> optionalTranslation = translationRepo.findByIdOptional(translation.id);

    if (optionalTranslation.isEmpty()) {
      throw new BadRequestException("Translation not found with id: " + translation.id);
    }

    Translation existing = optionalTranslation.get();

    if (translation.getValue() != null) {
      existing.setValue(translation.getValue());
    }

    if (translation.getActive() != null) {
      existing.setActive(translation.getActive());
    }

    translationRepo.persist(existing);
    return existing;
  }

  /**
   * deleteLanguage.
   *
   * @param language a {@link java.lang.String} object
   */
  @Transactional
  public void deleteLanguage(String language) {
    // Prevent English language from deletion
    if ("en".equals(language)) {
      throw new BadRequestException("Cannot delete English language");
    }

    long deletedCount = translationRepo.deleteByLanguage(language);
    log.infov("Deleted {0} translations for language {1}", deletedCount, language);
  }
}
