package com.cdw.cdw.configuration;


import com.cdw.cdw.service.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DatabaseMessageSource extends AbstractMessageSource {

    @Autowired
    private TranslationService translationService;

    private final Map<String, Map<String, String>> translationsCache = new ConcurrentHashMap<>();

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        String message = getText(code, locale);

        if (message == null) {
            // Nếu không tìm thấy bản dịch, trả về chính mã đó
            return new MessageFormat(code, locale);
        }

        return new MessageFormat(message, locale);
    }

    private String getText(String code, Locale locale) {
        String languageCode = locale.getLanguage();

        // Kiểm tra cache trước
        if (!translationsCache.containsKey(languageCode)) {
            // Nếu chưa có trong cache, tải từ service
            translationsCache.put(languageCode, translationService.getAllTranslations(languageCode));
        }

        Map<String, String> translations = translationsCache.get(languageCode);
        return translations.get(code);
    }

    public void clearCache() {
        translationsCache.clear();
    }
}
