package com.cdw.cdw.exception;

import com.cdw.cdw.domain.dto.request.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestControllerAdvice
public class GlobalException {

    @Autowired
    private MessageSource messageSource;

    private static final Logger logger = Logger.getLogger(GlobalException.class.getName());

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        ApiResponse<Map<String, String>> response = new ApiResponse<>();
        Map<String, String> errors = new HashMap<>();
        StringBuilder messageBuilder = new StringBuilder();

        BindingResult bindingResult = ex.getBindingResult();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            String messageKey = fieldError.getDefaultMessage();

            // Xử lý trường hợp messageKey có dạng {key.name}
            if (messageKey != null && messageKey.startsWith("{") && messageKey.endsWith("}")) {
                messageKey = messageKey.substring(1, messageKey.length() - 1);
            }

            String errorMessage = messageSource.getMessage(
                    messageKey,
                    fieldError.getArguments(),
                    fieldError.getDefaultMessage(),
                    LocaleContextHolder.getLocale());

            errors.put(fieldError.getField(), errorMessage);
            messageBuilder.append(fieldError.getField()).append(": ").append(errorMessage).append("; ");
        }

        response.setSuccess(false);
        if (messageBuilder.length() > 0) {
            response.setMessage(messageBuilder.substring(0, messageBuilder.length() - 2));
        } else {
            response.setMessage(messageSource.getMessage(
                    "validation.failed",
                    null,
                    "Validation failed",
                    LocaleContextHolder.getLocale()));
        }
        response.setResult(errors);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleAppException(AppException ex) {
        ApiResponse<Map<String, String>> response = new ApiResponse<>();
        Map<String, String> errors = new HashMap<>();

        String message = messageSource.getMessage(
                ex.getMessageKey(),
                null,
                ex.getMessageKey(),
                LocaleContextHolder.getLocale());

        // Ánh xạ messageKey đến tên trường
        String fieldName = mapErrorKeyToFieldName(ex.getMessageKey());

        if (fieldName != null) {
            errors.put(fieldName, message);
            response.setMessage(fieldName + ": " + message);
        } else {
            errors.put("global", message);
            response.setMessage(message);
        }

        response.setSuccess(false);
        response.setResult(errors);

        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }

    private String mapErrorKeyToFieldName(String errorKey) {
        Map<String, String> errorToFieldMap = new HashMap<>();
        errorToFieldMap.put("user.existed", "username");
        errorToFieldMap.put("email.existed", "email");
        errorToFieldMap.put("phone.existed", "phoneNumber");
        errorToFieldMap.put("password.invalid", "password");
        errorToFieldMap.put("password.mismatch", "confirmPassword");
        errorToFieldMap.put("user.not.found", "email");
        errorToFieldMap.put("invalid.token", "token");

        return errorToFieldMap.get(errorKey);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        logger.log(Level.SEVERE, "Unexpected error", ex);

        ApiResponse<Object> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(messageSource.getMessage(
                "internal.server.error",
                null,
                "Internal server error",
                LocaleContextHolder.getLocale()));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
