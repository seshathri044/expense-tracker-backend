package com.example.ExpenseTracker.SpringConfig;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.ExpenseTracker.Filter.JwtRequestFilter;
import com.example.ExpenseTracker.Service.AppUserDetialsService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final AppUserDetialsService appUserDetialsService;
    private final JwtRequestFilter jwtRequestFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // ✅ CRITICAL FIX: Export endpoints WITHOUT /api prefix
                        // Since context path is /api, Spring Security sees paths as /export/csv
                        .requestMatchers(
        "/login",
        "/register",
        "/send-reset-otp",
        "/reset-password",
        "/verify-otp",
        "/send-otp",
        "/logout",
        "/is-authenticated",
        "/error",
        "/auth/login",
        "/auth/register",
        "/auth/send-reset-otp",
        "/auth/reset-password",
        "/auth/verify-otp",
        "/auth/send-otp",
        "/auth/logout",
        // ✅ ADD THESE — context path is '/' so Spring Security sees full /api/ path
        "/api/login",
        "/api/register",
        "/api/send-reset-otp",
        "/api/reset-password",
        "/api/verify-otp",
        "/api/send-otp",
        "/api/logout",
        "/api/is-authenticated",
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/send-reset-otp",
        "/api/auth/reset-password",
        "/api/auth/verify-otp",
        "/api/auth/send-otp",
        "/api/auth/logout"
).permitAll()
                        // Swagger UI endpoints
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()
                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )
                .logout(logout -> logout.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(customAuthenticationEntryPoint));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Set-Cookie", "Authorization", "Content-Disposition")); // ✅ Added Content-Disposition for file downloads
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(appUserDetialsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(authenticationProvider);
    }
}