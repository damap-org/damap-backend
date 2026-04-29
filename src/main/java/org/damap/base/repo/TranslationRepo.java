package org.damap.base.repo;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.damap.base.domain.Translation;

/** TranslationRepo class. */
@ApplicationScoped
public class TranslationRepo implements PanacheRepository<Translation> {

  /**
   * findByLanguageAndActive.
   *
   * @param language a {@link java.lang.String} object
   * @param active a {@link java.lang.Boolean} object
   * @return a {@link java.util.List} object
   */
  public List<Translation> findByLanguageAndActive(String language, Boolean active) {
    return list("language = ?1 and active = ?2", Sort.by("translationKey"), language, active);
  }

  /**
   * findByLanguage.
   *
   * @param language a {@link java.lang.String} object
   * @return a {@link java.util.List} object
   */
  public List<Translation> findByLanguage(String language) {
    return list("language = ?1", Sort.by("translationKey"), language);
  }

  /**
   * findByKeyAndLanguage.
   *
   * @param key a {@link java.lang.String} object
   * @param language a {@link java.lang.String} object
   * @return an {@link java.util.Optional} object
   */
  public Optional<Translation> findByKeyAndLanguage(String key, String language) {
    return find("translationKey = ?1 and language = ?2", key, language).firstResultOptional();
  }

  /**
   * findAllLanguages.
   *
   * <p>Returns all distinct language codes present in the translation table, ordered
   * alphabetically. Runs the aggregation at the database level.
   *
   * @return a {@link java.util.List} of distinct language code strings
   */
  public List<String> findAllLanguages() {
    return getEntityManager()
        .createQuery(
            "SELECT DISTINCT t.language FROM Translation t ORDER BY t.language", String.class)
        .getResultList();
  }

  /**
   * deleteByLanguage.
   *
   * <p>WARNING: This is a destructive operation. It permanently deletes ALL translation entries for
   * the given language from the database and cannot be undone.
   *
   * @param language a {@link java.lang.String} language code
   * @return the number of deleted translation entities
   */
  @Transactional
  public long deleteByLanguage(String language) {
    return delete("language", language);
  }
}
