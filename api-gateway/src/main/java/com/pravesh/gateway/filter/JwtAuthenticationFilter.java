package com.pravesh.gateway.filter;

import com.pravesh.gateway.security.JwtValidator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    /** Paths that never require a token. */
    private static final List<String> OPEN_PATHS = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/forgot-password",
            "/api/auth/verify-otp",
            "/api/auth/reset-password",
            "/api/internal/",
            "/api/payments/webhook",
            "/ws"
    );

    private final JwtValidator jwtValidator;

    public JwtAuthenticationFilter(JwtValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (isOpen(path)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing or malformed Authorization header");
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtValidator.parse(token);

            ServerHttpRequest mutated = request.mutate()
                    .header("X-User-Id", String.valueOf(claims.get("userId")))
                    .header("X-User-Email", claims.getSubject())
                    .header("X-User-Role", String.valueOf(claims.get("role")))
                    .header("X-Verification-Status",
                            claims.get("verificationStatus") == null
                                    ? "" : String.valueOf(claims.get("verificationStatus")))
                    .build();

            return chain.filter(exchange.mutate().request(mutated).build());

        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("JWT rejected for {}: {}", path, ex.getMessage());
            return unauthorized(exchange, "Invalid or expired token");
        }
    }

    private boolean isOpen(String path) {
        return OPEN_PATHS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = """
                {"success":false,"message":"%s","path":"%s"}"""
                .formatted(message, exchange.getRequest().getURI().getPath());

        DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1; // run before routing
    }
}