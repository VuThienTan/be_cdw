package com.cdw.cdw.controller;

import com.cdw.cdw.domain.dto.response.AuthenticationResponse;
import com.cdw.cdw.domain.entity.Role;
import com.cdw.cdw.domain.entity.User;
import com.cdw.cdw.mapper.UserMapper;
import com.cdw.cdw.repository.RoleRepository;
import com.cdw.cdw.repository.UserRepository;
import com.cdw.cdw.service.AuthenticationService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/oauth/github")
@RequiredArgsConstructor
public class GithubOAuthController {

    @Value("${GITHUB_CLIENT_ID}")
    private String clientId;

    @Value("${GITHUB_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${GITHUB_REDIRECT_URI}")
    private String redirectUri;

    private final RestTemplate restTemplate = new RestTemplate();
    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    @GetMapping("/callback")
    public void githubCallback(@RequestParam String code, HttpServletResponse servletResponse) {
        try {
            // 1. Exchange code for access token
            String accessTokenUrl = "https://github.com/login/oauth/access_token";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("code", code);
            body.add("redirect_uri", redirectUri);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(accessTokenUrl, request, Map.class);
            String accessToken = (String) response.getBody().get("access_token");
            if (accessToken == null) {
                servletResponse.sendRedirect("http://localhost:3000/login?error=github_token");
                return;
            }

            // 2. Use access token to get GitHub user info
            HttpHeaders authHeaders = new HttpHeaders();
            authHeaders.setBearerAuth(accessToken);
            HttpEntity<Void> entity = new HttpEntity<>(authHeaders);
            ResponseEntity<Map> userInfo = restTemplate.exchange(
                    "https://api.github.com/user",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );
            Map<String, Object> userData = userInfo.getBody();
            if (userData == null || userData.get("email") == null) {
                // Try to fetch emails endpoint if email is not public
                ResponseEntity<List> emailsResp = restTemplate.exchange(
                        "https://api.github.com/user/emails",
                        HttpMethod.GET,
                        entity,
                        List.class
                );
                List<Map<String, Object>> emails = emailsResp.getBody();
                if (emails != null) {
                    for (Map<String, Object> emailObj : emails) {
                        if (Boolean.TRUE.equals(emailObj.get("primary")) && Boolean.TRUE.equals(emailObj.get("verified"))) {
                            userData.put("email", emailObj.get("email"));
                            break;
                        }
                    }
                }
            }
            String email = (String) userData.get("email");
            if (email == null) {
                servletResponse.sendRedirect("http://localhost:3000/login?error=github_email");
                return;
            }
            String username = (String) userData.getOrDefault("login", email.split("@")[0]);
            String fullName = (String) userData.getOrDefault("name", username);

            // 3. Find or create user
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                // Assign default role USER
                Role role = roleRepository.findById("USER").orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName("USER");
                    newRole.setDescription("Default role for registered users");
                    return roleRepository.save(newRole);
                });
                user = User.builder()
                        .username(username)
                        .email(email)
                        .passwordHash(UUID.randomUUID().toString()) // random password
                        .fullName(fullName)
                        .roles(Set.of(role))
                        .active(true)
                        .codeActive(UUID.randomUUID().toString())
                        .codeExpired(Date.from(Instant.now().plus(100, ChronoUnit.YEARS)))
                        .build();
                user = userRepository.save(user);
            } else if (!user.isActive()) {
                user.setActive(true);
                userRepository.save(user);
            }

            // 4. Generate JWT and set cookie
            String token = authenticationService.genarateToken(user);
            servletResponse.addCookie(authenticationService.createJwtCookie(token));

            // 5. Redirect về FE home, truyền token qua query string nếu muốn
            servletResponse.sendRedirect("http://localhost:3000?token=" + token);
        } catch (Exception e) {
            try {
                servletResponse.sendRedirect("http://localhost:3000/login?error=github_callback");
            } catch (Exception ignored) {}
        }
    }
}
