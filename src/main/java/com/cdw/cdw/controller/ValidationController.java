package com.cdw.cdw.controller;

import com.cdw.cdw.domain.dto.request.UserCreateRequest;
import com.cdw.cdw.service.UserValidatorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ValidationController {

    @Autowired
    private UserValidatorService userValidator;

    @Autowired
    private MessageSource messageSource;

    @InitBinder("userCreateRequest")
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(userValidator);
    }

    @PostMapping("/validate-user")
    public ResponseEntity<?> validateUser(@RequestBody @Valid UserCreateRequest request, BindingResult bindingResult, Locale locale) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                String errorMessage = messageSource.getMessage(error, locale);
                errors.put(error.getField(), errorMessage);
            }
            return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "errors", errors
            ));
        }

        return ResponseEntity.ok(Map.of("valid", true));
    }
}
