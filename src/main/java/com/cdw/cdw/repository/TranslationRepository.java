package com.cdw.cdw.repository;

import com.cdw.cdw.domain.entity.Language;
import com.cdw.cdw.domain.entity.Translation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TranslationRepository extends JpaRepository<Translation, Long> {
    List<Translation> findByLanguage(Language language);

    Optional<Translation> findByLanguageAndKey(Language language, String key);

    @Query("SELECT t FROM Translation t WHERE t.language.code = :languageCode")
    List<Translation> findByLanguageCode(String languageCode);
}