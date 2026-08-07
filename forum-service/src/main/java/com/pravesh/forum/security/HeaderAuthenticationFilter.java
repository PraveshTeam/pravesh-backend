package com.pravesh.forum.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// Trusts X-User-* headers because they can ONLY be set by the gateway's
// JwtAuthenticationFilter after it has already validated the JWT -- this
// service is never reachable directly from the browser, only via the gateway.
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        String userId = request.getHeader("X-User-Id");
        String email = request.getHeader("X-User-Email");
        String role = request.getHeader("X-User-Role");
        String societyId = request.getHeader("X-Society-Id");

        if (userId != null && role != null
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            AuthenticatedUser principal = new AuthenticatedUser(
                    Long.parseLong(userId), email, role,
                    (societyId != null && !societyId.isBlank()) ? Long.parseLong(societyId) : null);

            var auth = new UsernamePasswordAuthenticationToken(
                    principal, null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role)));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}
