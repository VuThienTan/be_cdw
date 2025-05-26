package com.cdw.cdw.controller;

import com.cdw.cdw.domain.dto.request.ApiResponse;
import com.cdw.cdw.domain.dto.request.ForgotPasswordRequest;
import com.cdw.cdw.domain.dto.request.ResetPasswordRequest;
import com.cdw.cdw.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PasswordResetController {

    private final UserService userService;

    @PostMapping("/forgot-password")
    public ApiResponse<String> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        userService.processForgotPassword(request);
        ApiResponse<String> response = new ApiResponse<>();
        response.setResult("Email đặt lại mật khẩu đã được gửi thành công!");
        return response;
    }

    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        userService.resetPassword(request);
        ApiResponse<String> response = new ApiResponse<>();
        response.setResult("Đặt lại mật khẩu thành công!");
        return response;
    }
}
