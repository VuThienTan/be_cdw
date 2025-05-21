package com.cdw.cdw.configuration;

import com.cdw.cdw.domain.dto.request.IntrospectRequest;
import com.cdw.cdw.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.text.ParseException;

@Component
public class CustomJwtDecoder implements JwtDecoder {

    private final NimbusJwtDecoder jwtDecoder;
    private final AuthenticationService authenticationService;

    public CustomJwtDecoder(
            @Value("${spring.jwt.signerKey}") String signerKey,
            AuthenticationService authenticationService
    ) {
        SecretKeySpec secretKey = new SecretKeySpec(signerKey.getBytes(), "HmacSHA256");

        this.jwtDecoder = NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        this.authenticationService = authenticationService;
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            var request = IntrospectRequest.builder().token(token).build();
            var response = authenticationService.introspect(request);

            if (!response.isValid()) {
                throw new JwtException("Token is invalid (introspection failed)");
            }

            return jwtDecoder.decode(token);

        } catch (JOSEException | ParseException e) {
            throw new JwtException("Token decoding failed");
        }
    }
}
