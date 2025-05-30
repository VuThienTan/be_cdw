package com.cdw.cdw.service;

import com.cdw.cdw.domain.dto.request.AuthenticationRequest;
import com.cdw.cdw.domain.dto.request.IntrospectRequest;
import com.cdw.cdw.domain.dto.request.LogoutRequest;
import com.cdw.cdw.domain.dto.response.AuthenticationResponse;
import com.cdw.cdw.domain.dto.response.IntrospectResponse;
import com.cdw.cdw.domain.entity.InvalidatedToken;
import com.cdw.cdw.domain.entity.User;
import com.cdw.cdw.exception.AppException;
import com.cdw.cdw.exception.ErrorCode;
import com.cdw.cdw.repository.InvalidatedTokenRepository;
import com.cdw.cdw.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.cdw.cdw.exception.ErrorCode.USER_EXISTED;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    EmailService emailService;
    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;

    @NonFinal
    @Value("${spring.jwt.signerKey}")
    protected String SIGNER_KEY;

    public AuthenticationResponse authenticate(AuthenticationRequest request, HttpServletResponse response) {
        final User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if(!user.isActive()){
            throw new AppException(ErrorCode.ACCOUNT_NOT_ACTIVE);
        }

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
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
                .orElseThrow(() -> new AppException(ErrorCode.ACTIVE_CODE_NOT_FOUND));

        if (user.isActive()) {
            throw new AppException(ErrorCode.ACCOUNT_IS_ACTIVE);
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
                throw new AppException(ErrorCode.EMAIL_SENDING_ERROR);
            }

            throw new AppException(ErrorCode.TIME_EXPIRED);
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
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

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
