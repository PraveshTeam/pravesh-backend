package com.pravesh.user.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

    	String userId = request.getHeader("X-User-Id");
        String role = request.getHeader("X-User-Role");
        String email = request.getHeader("X-User-Email");
        String societyIdHeader = request.getHeader("X-Society-Id");

        if (userId != null && role != null
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            Long societyId = (societyIdHeader != null && !societyIdHeader.isBlank())
                    ? Long.valueOf(societyIdHeader) : null;

            var authority = new SimpleGrantedAuthority("ROLE_" + role);
            var authentication = new UsernamePasswordAuthenticationToken(
                    new AuthenticatedUser(Long.valueOf(userId), email, role, societyId),
                    null, List.of(authority));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}