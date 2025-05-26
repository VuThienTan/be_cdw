package com.cdw.cdw.configuration;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class PublicEndpointConfig {

    public final String[] POST_PUBLIC_ENDPOINTS = {
            "/users",
            "/auths/login", "/auths/introspect", "/auths/logout",
            "/forgot-password", "/reset-password"
    };

    public final String[] GET_PUBLIC_ENDPOINTS = {
            "/menuItem", "/menuItem/*"
    };

    public List<String> getPublicEndpoints() {
        List<String> all = new java.util.ArrayList<>();
        all.addAll(List.of(POST_PUBLIC_ENDPOINTS));
        all.addAll(List.of(GET_PUBLIC_ENDPOINTS));
        return all;
    }
}
