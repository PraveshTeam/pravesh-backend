package com.pravesh.validation.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${pravesh.internal.api-key}")
    private String internalApiKey;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/internal/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(new InternalApiKeyFilter(internalApiKey),
                    UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new HeaderAuthenticationFilter(),
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}