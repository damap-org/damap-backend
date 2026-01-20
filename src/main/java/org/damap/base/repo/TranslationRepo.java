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
    return list("language = ?1 and active = ?2", Sort.by("key"), language, active);
  }

  /**
   * findByLanguage.
   *
   * @param language a {@link java.lang.String} object
   * @return a {@link java.util.List} object
   */
  public List<Translation> findByLanguage(String language) {
    return list("language = ?1", Sort.by("key"), language);
  }

  /**
   * findByKeyAndLanguage.
   *
   * @param key a {@link java.lang.String} object
   * @param language a {@link java.lang.String} object
   * @return an {@link java.util.Optional} object
   */
  public Optional<Translation> findByKeyAndLanguage(String key, String language) {
    return find("key = ?1 and language = ?2", key, language).firstResultOptional();
  }

  /**
   * deleteByLanguage.
   *
   * @param language a {@link java.lang.String} object
   * @return number of deleted entities
   */
  @Transactional
  public long deleteByLanguage(String language) {
    return delete("language", language);
  }
}
