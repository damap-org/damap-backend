package org.damap.base.rest.storage;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import lombok.experimental.UtilityClass;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.domain.InternalStorage;
import org.damap.base.domain.InternalStorageTranslation;
import org.damap.base.repo.InternalStorageRepo;
import org.damap.base.repo.InternalStorageTranslationRepo;
import org.damap.base.rest.translation.service.LanguageCodeValidator;

@UtilityClass
@JBossLog
public class InternalStorageTranslationValidator {

  public void validateForCreation(
      InternalStorageTranslationDO internalStorageTranslationDO,
      InternalStorageRepo internalStorageRepo,
      InternalStorageTranslationRepo internalStorageTranslationRepo)
      throws ClientErrorException {
    log.info("Validating internal storage translation for creation");

    normalizeAndValidateLanguageCode(internalStorageTranslationDO);

    // Check if no translation for the same language exists
    if (internalStorageTranslationRepo.existsTranslationForStorageIdAndLanguageCode(
        internalStorageTranslationDO.getStorageId(),
        internalStorageTranslationDO.getLanguageCode())) {
      throw new ClientErrorException(
          "Translation for language code "
              + internalStorageTranslationDO.getLanguageCode()
              + " already exists",
          Response.Status.BAD_REQUEST);
    }

    InternalStorageTranslationValidator.validationCommon(
        internalStorageTranslationDO, internalStorageRepo);
  }

  public void validateForUpdate(
      String id,
      InternalStorageTranslationDO internalStorageTranslationDO,
      InternalStorageTranslationRepo internalStorageTranslationRepo,
      InternalStorageRepo internalStorageRepo)
      throws ClientErrorException {
    log.info("Validating internal storage translation for update");

    normalizeAndValidateLanguageCode(internalStorageTranslationDO);

    InternalStorageTranslation internalStorageTranslation =
        internalStorageTranslationRepo.findById(Long.parseLong(id));

    if (internalStorageTranslation == null) {
      throw new NotFoundException("No internal storage translation with ID " + id + " found");
    }

    if (internalStorageTranslationRepo.existsTranslationForStorageIdAndLanguageCodeExceptId(
        internalStorageTranslationDO.getStorageId(),
        internalStorageTranslationDO.getLanguageCode(),
        Long.parseLong(id))) {
      throw new ClientErrorException(
          "Translation for language code "
              + internalStorageTranslationDO.getLanguageCode()
              + " already exists",
          Response.Status.BAD_REQUEST);
    }

    InternalStorageTranslationValidator.validationCommon(
        internalStorageTranslationDO, internalStorageRepo);
  }

  public void validationCommon(
      InternalStorageTranslationDO internalStorageTranslationDO,
      InternalStorageRepo internalStorageRepo)
      throws ClientErrorException {
    log.info("Validating common internal storage translation");

    InternalStorage internalStorage =
        internalStorageRepo.findById(internalStorageTranslationDO.getStorageId());

    if (internalStorage == null) {
      throw new NotFoundException(
          "No internal storage found for id " + internalStorageTranslationDO.getStorageId());
    }

    normalizeAndValidateLanguageCode(internalStorageTranslationDO);
  }

  private void normalizeAndValidateLanguageCode(
      InternalStorageTranslationDO internalStorageTranslationDO) {
    String languageCode = internalStorageTranslationDO.getLanguageCode();

    if (languageCode == null || languageCode.isBlank()) {
      throw new ClientErrorException(
          "Language code must not be empty", Response.Status.BAD_REQUEST);
    }

    String normalizedLanguageCode = languageCode.trim().toLowerCase();

    if (!LanguageCodeValidator.isValidIso6391Code(normalizedLanguageCode)) {
      throw new ClientErrorException(
          "Invalid ISO 639-1 language code: " + languageCode, Response.Status.BAD_REQUEST);
    }

    internalStorageTranslationDO.setLanguageCode(normalizedLanguageCode);
  }
}
