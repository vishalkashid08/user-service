package com.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // ❌ COMPLETELY DISABLE CORS HANDLING IN SECURITY
            .cors(cors -> cors.disable())

            // ❌ DISABLE CSRF
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth
                    // ✅ VERY IMPORTANT (ALLOW PREFLIGHT)
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                    // ✅ PUBLIC ENDPOINTS
                    .requestMatchers("/auth/**").permitAll()
                    .requestMatchers("/users/name/**").permitAll()

                    // ✅ ADMIN ONLY
                    .requestMatchers("/users/**").hasRole("ADMIN")

                    .anyRequest().authenticated()
            )

            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // ✅ JWT FILTER
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());

        return http.build();
    }
}