package com.cdw.cdw.service;


import com.cdw.cdw.domain.entity.Language;
import com.cdw.cdw.domain.entity.Translation;

import com.cdw.cdw.repository.LanguageRepository;
import com.cdw.cdw.repository.TranslationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TranslationService {

    @Autowired
    private TranslationRepository translationRepository;

    @Autowired
    private LanguageRepository languageRepository;

    @Cacheable(value = "translationsCache", key = "#languageCode")
    public Map<String, String> getAllTranslations(String languageCode) {
        Map<String, String> translations = new HashMap<>();


        Optional<Language> language = languageRepository.findByCode(languageCode);


        if (language.isEmpty()) {
            language = languageRepository.findByIsDefaultTrue();
        }


        if (language.isPresent()) {
            List<Translation> translationList = translationRepository.findByLanguage(language.get());


            for (Translation translation : translationList) {
                translations.put(translation.getKey(), translation.getValue());
            }
        }

        return translations;
    }


}

