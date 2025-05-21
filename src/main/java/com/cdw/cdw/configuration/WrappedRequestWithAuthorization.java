package com.cdw.cdw.configuration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.*;

public class WrappedRequestWithAuthorization extends HttpServletRequestWrapper {

    private final String bearerToken;

    public WrappedRequestWithAuthorization(HttpServletRequest request, String bearerToken) {
        super(request);
        this.bearerToken = bearerToken;
    }

    @Override
    public String getHeader(String name) {
        if ("Authorization".equalsIgnoreCase(name)) {
            return bearerToken;
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        if ("Authorization".equalsIgnoreCase(name)) {
            return Collections.enumeration(List.of(bearerToken));
        }
        return super.getHeaders(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        List<String> names = Collections.list(super.getHeaderNames());
        if (!names.contains("Authorization")) {
            names.add("Authorization");
        }
        return Collections.enumeration(names);
    }
}
