package com.cdw.cdw.service;

import com.cdw.cdw.domain.dto.request.ForgotPasswordRequest;
import com.cdw.cdw.domain.dto.request.ResetPasswordRequest;
import com.cdw.cdw.domain.dto.request.UserCreateRequest;
import com.cdw.cdw.domain.dto.response.UserResponse;
import com.cdw.cdw.domain.entity.Role;
import com.cdw.cdw.domain.entity.User;
import com.cdw.cdw.exception.AppException;
import com.cdw.cdw.mapper.UserMapper;
import com.cdw.cdw.repository.RoleRepository;
import com.cdw.cdw.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    EmailService emailService;
    JwtPasswordResetService jwtPasswordResetService;

    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw AppException.badRequest("email.existed");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw AppException.badRequest("user.existed");
        }

        Role role = roleRepository.findById("USER")
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName("USER");
                    newRole.setDescription("Default role for registered users");
                    return roleRepository.save(newRole);
                });
        String code = UUID.randomUUID().toString();

        try {
            emailService.sendAccountActivationEmail(request.getEmail(), code, request.getFullName());
        } catch (MessagingException e) {
            throw AppException.serverError("email.sending.error");
        }

        User user = userMapper.toUser(request).toBuilder()
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(role))
                .codeActive(code)
                .codeExpired(Date.from(Instant.now().plus(15, ChronoUnit.MINUTES)))
                .build();

        User savedUser = userRepository.save(user);
        return userMapper.toUserResponse(savedUser);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public List<UserResponse> getAllUser() {
        return userMapper.toUserResponse(userRepository.findAll());
    }

    @PostAuthorize("returnObject.username == authentication.name || hasAuthority('ADMIN')")
    public UserResponse getUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("user.not.found"));

        return userMapper.toUserResponse(user);
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> AppException.notFound("user.not.found"));

        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public UserResponse deleteUser(String id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            userRepository.deleteById(id);
            return userMapper.toUserResponse(user);
        }
        return null;
    }

    public void processForgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> AppException.notFound("user.not.found"));

        String token = jwtPasswordResetService.generatePasswordResetToken(email);

        try {
            emailService.sendPasswordResetEmail(email, token);
        } catch (MessagingException e) {
            throw AppException.serverError("email.sending.error");
        }
    }

    public void resetPassword(ResetPasswordRequest request) {
        if (!jwtPasswordResetService.validateToken(request.getToken())) {
            throw AppException.badRequest("invalid.token");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw AppException.badRequest("password.mismatch");
        }

        String email = jwtPasswordResetService.extractEmail(request.getToken());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> AppException.notFound("user.not.found"));

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
