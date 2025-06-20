package com.cdw.cdw.configuration;

import com.cdw.cdw.domain.dto.response.AuthenticationResponse;
import com.cdw.cdw.service.AuthenticationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthenticationService authenticationService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        try {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = oauthToken.getPrincipal();


            String email = oauth2User.getAttribute("email");
            String name = oauth2User.getAttribute("name");

            if (email == null) {
                getRedirectStrategy().sendRedirect(request, response, "/cdw/login?error=email_not_provided");
                return;
            }

            // Xác thực người dùng và tạo token
            AuthenticationResponse authResponse = authenticationService.authenticateWithGoogle(email, name);

            // Thêm token vào cookie
            Cookie jwtCookie = new Cookie("JWT_TOKEN", authResponse.getToken());
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(7 * 24 * 60 * 60); // 7 ngày
            response.addCookie(jwtCookie);


            getRedirectStrategy().sendRedirect(request, response, "http://localhost:3000");
        } catch (Exception e) {
            getRedirectStrategy().sendRedirect(request, response, "/cdw/login?error=authentication_failed");
        }
    }


}

