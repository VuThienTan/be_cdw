//package com.cdw.cdw.controller;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.Collections;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/oauth/github")
//public class GithubOAuthController {
//
//    @Value("${GITHUB_CLIENT_ID}")
//    private String clientId;
//
//    @Value("${GITHUB_CLIENT_SECRET}")
//    private String clientSecret;
//
//    @Value("${GITHUB_REDIRECT_URI}")
//    private String redirectUri;
//
//    private final RestTemplate restTemplate = new RestTemplate();
//
//    @GetMapping("/callback")
//    public ResponseEntity<?> githubCallback(@RequestParam String code) {
//        // 1. Đổi code lấy access token
//        String accessTokenUrl = "https://github.com/login/oauth/access_token";
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
//
//        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
//        body.add("client_id", clientId);
//        body.add("client_secret", clientSecret);
//        body.add("code", code);
//        body.add("redirect_uri", redirectUri);
//
//        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
//
//        ResponseEntity<Map> response = restTemplate.postForEntity(accessTokenUrl, request, Map.class);
//        String accessToken = (String) response.getBody().get("access_token");
//
//        // 2. Dùng access token để lấy thông tin user GitHub
//        HttpHeaders authHeaders = new HttpHeaders();
//        authHeaders.setBearerAuth(accessToken);
//        HttpEntity<Void> entity = new HttpEntity<>(authHeaders);
//
//        ResponseEntity<Map> userInfo = restTemplate.exchange(
//                "https://api.github.com/user",
//                HttpMethod.GET,
//                entity,
//                Map.class
//        );
//
//        // 3. Trích xuất thông tin và xử lý đăng nhập (tùy hệ thống của bạn)
//        Map<String, Object> userData = userInfo.getBody();
//        return ResponseEntity.ok(userData);
//    }
//}
