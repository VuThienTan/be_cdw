package com.cdw.cdw.controller;

import com.cdw.cdw.configuration.DatabaseMessageSource;
import com.cdw.cdw.domain.dto.request.ApiResponse;
import com.cdw.cdw.domain.dto.request.TranslationRequest;
import com.cdw.cdw.domain.entity.Language;
import com.cdw.cdw.domain.entity.Translation;
import com.cdw.cdw.repository.LanguageRepository;
import com.cdw.cdw.repository.TranslationRepository;
import com.cdw.cdw.service.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/translations")
public class TranslationController {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private LanguageRepository languageRepository;

    @Autowired
    private TranslationRepository translationRepository;

    @Autowired
    private DatabaseMessageSource databaseMessageSource;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> getTranslations(@RequestParam String lang) {
        Map<String, String> translations = translationService.getAllTranslations(lang);
        return ResponseEntity.ok(new ApiResponse<>("Lấy bản dịch thành công", translations, true));
    }

    @GetMapping("/languages")
    public ResponseEntity<ApiResponse<List<Language>>> getLanguages() {
        List<Language> languages = languageRepository.findAll();
        return ResponseEntity.ok(new ApiResponse<>("Lấy danh sách ngôn ngữ thành công", languages, true));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Translation>> updateTranslation(@RequestBody TranslationRequest request) {
        Optional<Language> language = languageRepository.findByCode(request.getLanguageCode());

        if (language.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("Ngôn ngữ không tồn tại", null, false));
        }

        Optional<Translation> existingTranslation =
                translationRepository.findByLanguageAndKey(language.get(), request.getKey());

        Translation translation;
        if (existingTranslation.isPresent()) {
            translation = existingTranslation.get();
            translation.setValue(request.getValue());
        } else {
            translation = new Translation();
            translation.setLanguage(language.get());
            translation.setKey(request.getKey());
            translation.setValue(request.getValue());
        }

        Translation savedTranslation = translationRepository.save(translation);

        // Xóa cache để cập nhật bản dịch mới
        databaseMessageSource.clearCache();

        return ResponseEntity.ok(new ApiResponse<>("Cập nhật bản dịch thành công", savedTranslation, true));
    }
}
