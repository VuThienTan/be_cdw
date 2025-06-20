package com.cdw.cdw.validation;

import com.cdw.cdw.domain.dto.request.UserCreateRequest;
import com.cdw.cdw.exception.AppException;
import com.cdw.cdw.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class UserValidator implements Validator {

    private final UserRepository userRepository;

    public UserValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return UserCreateRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UserCreateRequest request = (UserCreateRequest) target;

        // Kiểm tra email đã tồn tại chưa
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            errors.rejectValue("email", "email.existed", "Email đã được sử dụng");
        }

        // Kiểm tra username đã tồn tại chưa
        if (request.getUsername() != null && userRepository.existsByUsername(request.getUsername())) {
            errors.rejectValue("username", "user.existed", "Người dùng đã tồn tại");
        }

        // Kiểm tra mật khẩu và repassword
        if (request.getPassword() != null && request.getRepassword() != null
                && !request.getPassword().equals(request.getRepassword())) {
            errors.rejectValue("repassword", "password.mismatch", "Mật khẩu nhập lại không khớp");
        }

        // Kiểm tra độ dài mật khẩu
        if (request.getPassword() != null && (request.getPassword().length() < 8 || request.getPassword().length() > 20)) {
            errors.rejectValue("password", "invalid.password", "Mật khẩu phải từ 8-20 ký tự");
        }
    }
}
