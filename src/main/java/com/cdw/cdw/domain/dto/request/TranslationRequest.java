
package com.cdw.cdw.domain.dto.request;

import lombok.Data;

@Data
public class TranslationRequest {
    private String languageCode;
    private String key;
    private String value;
}
