package com.cdw.cdw.controller;

import com.cdw.cdw.domain.dto.request.ApiResponse;
import com.cdw.cdw.domain.dto.request.AuthenticationRequest;
import com.cdw.cdw.domain.dto.request.IntrospectRequest;
import com.cdw.cdw.domain.dto.request.LogoutRequest;
import com.cdw.cdw.domain.dto.response.AuthenticationResponse;
import com.cdw.cdw.domain.dto.response.IntrospectResponse;
import com.cdw.cdw.domain.dto.response.LogoutResponse;
import com.cdw.cdw.domain.entity.InvalidatedToken;
import com.cdw.cdw.domain.entity.Notification;
import com.cdw.cdw.domain.entity.User;
import com.cdw.cdw.repository.NotificationRepository;
import com.cdw.cdw.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("/auths")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;
    @Autowired
    NotificationRepository notificationRepository;

    @PostMapping("/login")
    public ApiResponse<AuthenticationResponse> authenticate(@RequestBody @Valid AuthenticationRequest authenticationRequest, HttpServletResponse response) {
        AuthenticationResponse result = authenticationService.authenticate(authenticationRequest, response);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @GetMapping("/activate")
    public ApiResponse<AuthenticationResponse> activateAccount(@RequestParam("code") String activationCode, HttpServletResponse response) {
        AuthenticationResponse result = authenticationService.activateUserAccount(activationCode, response);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/introspect")
    public ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest introspectRequest) throws ParseException, JOSEException {
        IntrospectResponse result = authenticationService.introspect(introspectRequest);
        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/logout")
    public ApiResponse<LogoutResponse> logout(@RequestBody(required = false) LogoutRequest logoutRequest, 
                                             HttpServletRequest request, 
                                             HttpServletResponse response) throws ParseException, JOSEException {
        authenticationService.logout(logoutRequest, request, response);
        return ApiResponse.<LogoutResponse>builder()
                .result(LogoutResponse.builder()
                        .success(true)
                        .message("Logout successful")
                        .build())
                .success(true)
                .build();
    }
    
    @GetMapping("/logout")
    public ApiResponse<LogoutResponse> logoutGet(HttpServletRequest request, 
                                                HttpServletResponse response) throws ParseException, JOSEException {
        authenticationService.logout(null, request, response);
        return ApiResponse.<LogoutResponse>builder()
                .result(LogoutResponse.builder()
                        .success(true)
                        .message("Logout successful")
                        .build())
                .success(true)
                .build();
    }

}
