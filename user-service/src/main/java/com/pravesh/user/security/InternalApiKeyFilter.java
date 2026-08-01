package com.pravesh.user.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class InternalApiKeyFilter extends OncePerRequestFilter {

    private final String expectedKey;

    public InternalApiKeyFilter(String expectedKey) {
        this.expectedKey = expectedKey;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.startsWith("/api/internal/")) {
            String providedKey = request.getHeader("X-Internal-Api-Key");
            if (providedKey == null || !providedKey.equals(expectedKey)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"success\":false,\"message\":\"Missing or invalid internal API key\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}