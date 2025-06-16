package com.cdw.cdw.service;

import com.cdw.cdw.domain.dto.request.AuthenticationRequest;
import com.cdw.cdw.domain.dto.request.IntrospectRequest;
import com.cdw.cdw.domain.dto.request.LogoutRequest;
import com.cdw.cdw.domain.dto.response.AuthenticationResponse;
import com.cdw.cdw.domain.dto.response.IntrospectResponse;
import com.cdw.cdw.domain.entity.InvalidatedToken;
import com.cdw.cdw.domain.entity.Role;
import com.cdw.cdw.domain.entity.User;
import com.cdw.cdw.exception.AppException;
import com.cdw.cdw.repository.InvalidatedTokenRepository;
import com.cdw.cdw.repository.RoleRepository;
import com.cdw.cdw.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    EmailService emailService;
    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    @NonFinal
    @Value("${spring.jwt.signerKey}")
    protected String SIGNER_KEY;
    @Transactional
    public AuthenticationResponse authenticateWithGoogle(String email, String name) {
        try {
            log.info("Đang xác thực với Google cho email: {}", email);

            // Sử dụng phương thức mới để tải eager roles
            User user = userRepository.findByEmailWithRoles(email)
                    .orElseGet(() -> {
                        log.info("Tạo người dùng mới cho email: {}", email);
                        // Nếu chưa tồn tại, tạo người dùng mới
                        Role role = roleRepository.findById("USER")
                                .orElseGet(() -> {
                                    Role newRole = new Role();
                                    newRole.setName("USER");
                                    newRole.setDescription("Default role for registered users");
                                    return roleRepository.save(newRole);
                                });

                        // Tạo người dùng mới với đầy đủ các trường bắt buộc
                        User newUser = User.builder()
                                .username(email)
                                .email(email)
                                .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                                .fullName(name != null ? name : "Google User")
                                .active(true)
                                .codeActive(UUID.randomUUID().toString())
                                .roles(Set.of(role))
                                .provider(User.AuthProvider.GOOGLE)
                                .build();

                        return userRepository.save(newUser);
                    });

            log.info("Người dùng đã được tìm thấy/tạo: {}", user.getUsername());

            // Tạo token cho người dùng
            final String token = genarateToken(user);
            log.info("Token đã được tạo thành công");

            return AuthenticationResponse.builder()
                    .token(token)
                    .success(true)
                    .build();
        } catch (Exception e) {
            log.error("Lỗi khi xác thực với Google: ", e);
            throw new RuntimeException("Không thể xác thực với Google: " + e.getMessage(), e);
        }
    }



    public AuthenticationResponse authenticate(AuthenticationRequest request, HttpServletResponse response) {
        final User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> AppException.notFound("user.not.found"));

        if(!user.isActive()){
            throw AppException.badRequest("account.not.active");
        }

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw AppException.unauthorized("unauthenticated");
        }

        final String token = genarateToken(user);
        response.addCookie(createJwtCookie(token));

        return AuthenticationResponse.builder()
                .token(token)
                .success(true)
                .build();
    }

    public AuthenticationResponse activateUserAccount(String activationCode, HttpServletResponse response) {
        final User user = userRepository.findByCodeActive(activationCode)
                .orElseThrow(() -> AppException.notFound("active.code.not.found"));

        if (user.isActive()) {
            throw AppException.badRequest("account.is.active");
        }

        //Gửi lại code nếu hết hạn
        if (user.getCodeExpired() != null && user.getCodeExpired().before(new Date())) {
            final String newCode = UUID.randomUUID().toString();
            final Date newExpire = Date.from(Instant.now().plus(15, ChronoUnit.MINUTES));

            user.setCodeActive(newCode);
            user.setCodeExpired(newExpire);
            userRepository.save(user);

            try {
                emailService.sendAccountActivationEmail(user.getEmail(), newCode, user.getFullName());
            } catch (MessagingException e) {
                throw AppException.serverError("email.sending.error");
            }

            throw AppException.badRequest("time.expired");
        }

        user.setActive(true);
        userRepository.save(user);

        final String token = genarateToken(user);
        response.addCookie(createJwtCookie(token));

        return AuthenticationResponse.builder()
                .token(token)
                .success(true)
                .build();
    }

    private Cookie createJwtCookie(String token) {
        Cookie jwtCookie = new Cookie("JWT_TOKEN", token);
        jwtCookie.setHttpOnly(true);
//      jwtCookie.setSecure(true); // bật ở môi trường production
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(7 * 24 * 60 * 60); // 7 ngày
        return jwtCookie;
    }

    public IntrospectResponse introspect(IntrospectRequest request)
            throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token);
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }

    private String genarateToken(User user) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("hcmuaf")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(10, ChronoUnit.HOURS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("cannot sign token", e);
            throw new RuntimeException(e);
        }
    }

    public void logout(LogoutRequest logoutRequest) throws ParseException, JOSEException {
        var signToken = verifyToken(logoutRequest.getToken());

        String jit = signToken.getJWTClaimsSet().getJWTID();
        Date exprireTime = signToken.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jit)
                .expirationTime(exprireTime)
                .build();

        invalidatedTokenRepository.save(invalidatedToken);
    }

    private SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verify = signedJWT.verify(verifier);

        if (!(verify && expiration.after(new Date()))) {
            throw AppException.unauthorized("unauthenticated");
        }

        // Check if JWT ID exists before checking invalidated tokens
        String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
        if (jwtId != null && invalidatedTokenRepository.existsById(jwtId)) {
            throw AppException.unauthorized("unauthenticated");
        }

        return signedJWT;
    }

    private String buildScope(User user) {
        StringJoiner scopeJoiner = new StringJoiner(" ");

        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(role -> {
                scopeJoiner.add(role.getName());
                if (!CollectionUtils.isEmpty(role.getPermissions())) {
                    role.getPermissions().forEach(permission -> {
                        scopeJoiner.add(permission.getName());
                    });
                }
            });
        }

        return scopeJoiner.toString();
    }
}
