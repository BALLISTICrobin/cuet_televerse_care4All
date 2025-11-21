package com.careforall.auth_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity; // FIX: Missing import
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; // FIX: Missing import
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // FIX: Missing import
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // FIX: Missing import
import org.springframework.security.crypto.password.PasswordEncoder; // FIX: Missing import
import org.springframework.security.web.SecurityFilterChain; // FIX: Missing import

@Configuration
@EnableWebSecurity
public class AuthConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/register", "/auth/token", "/auth/validate").permitAll()
                        .anyRequest().authenticated()
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}