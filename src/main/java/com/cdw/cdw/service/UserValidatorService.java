package com.cdw.cdw.service;

import com.cdw.cdw.domain.dto.request.UserCreateRequest;
import com.cdw.cdw.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class UserValidatorService implements Validator {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return UserCreateRequest.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UserCreateRequest request = (UserCreateRequest) target;

        // Kiểm tra username đã tồn tại chưa
        if (userRepository.existsByUsername(request.getUsername())) {
            errors.rejectValue("username", "username.exists", "Tên đăng nhập đã tồn tại");
        }

        // Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(request.getEmail())) {
            errors.rejectValue("email", "email.exists", "Email đã tồn tại");
        }
    }
}
