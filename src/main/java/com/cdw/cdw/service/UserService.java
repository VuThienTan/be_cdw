package com.cdw.cdw.service;

import com.cdw.cdw.domain.dto.request.ForgotPasswordRequest;
import com.cdw.cdw.domain.dto.request.ResetPasswordRequest;
import com.cdw.cdw.domain.dto.request.UserCreateRequest;
import com.cdw.cdw.domain.dto.request.UserUpdateRequest;
import com.cdw.cdw.domain.dto.response.UserResponse;
import com.cdw.cdw.domain.entity.Role;
import com.cdw.cdw.domain.entity.User;
import com.cdw.cdw.exception.AppException;
import com.cdw.cdw.mapper.UserMapper;
import com.cdw.cdw.repository.RoleRepository;
import com.cdw.cdw.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
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
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<UserResponse> getAllUser() {
        try {
            // Chỉ lấy người dùng có active = true
            List<User> activeUsers = userRepository.findByActiveTrue();
            return userMapper.toUserResponse(activeUsers);
        } catch (Exception e) {
            throw AppException.serverError("user.retrieve.error");
        }
    }


    @PreAuthorize("hasAuthority('ADMIN')")
    public UserResponse updateUser(String id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("user.not.found"));

        // Cập nhật thông tin người dùng
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw AppException.badRequest("user.existed");
            }
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw AppException.badRequest("email.existed");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        User savedUser = userRepository.save(user);
        return userMapper.toUserResponse(savedUser);
    }


//    softdelete
    @PreAuthorize("hasAuthority('ADMIN')")
    @Transactional
    public UserResponse deleteUser(String id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> AppException.notFound("user.not.found"));

            // Đặt trạng thái active = false thay vì xóa
            user.setActive(false);

            // Lưu người dùng với trạng thái đã cập nhật
            User savedUser = userRepository.save(user);


            return userMapper.toUserResponse(savedUser);
        } catch (Exception e) {

            throw AppException.serverError("user.delete.error");
        }
    }



}
