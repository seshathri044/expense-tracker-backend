package com.example.ExpenseTracker.Filter;

import com.example.ExpenseTracker.Service.AppUserDetialsService;
import com.example.ExpenseTracker.Util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final AppUserDetialsService appUserDetialsService;
    private final JwtUtil jwtUtil;

    // Paths WITHOUT /api/v1.0 prefix (context path is stripped by Spring)
    private static final List<String> PUBLIC_URLS = Arrays.asList(
            "/login",
            "/register",
            "/send-reset-otp",
            "/reset-password",
            "/verify-otp",
            "/logout",
            "/error"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String servletPath = request.getServletPath();

        log.debug("JwtRequestFilter: Processing request path: {}", servletPath);

        // Skip JWT validation for public endpoints
        if (PUBLIC_URLS.contains(servletPath)) {
            log.debug("JwtRequestFilter: Public URL detected, skipping JWT validation: {}", servletPath);
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = null;
        String email = null;

        // 1. Check Authorization header first
        final String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            log.debug("JwtRequestFilter: JWT token found in Authorization header");
        }

        // 2. If not in header, check cookies
        if (jwt == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("jwt".equals(cookie.getName())) {
                        jwt = cookie.getValue();
                        log.debug("JwtRequestFilter: JWT token found in cookie");
                        break;
                    }
                }
            }
        }

        // 3. Validate token and set authentication
        if (jwt != null) {
            try {
                email = jwtUtil.extractEmail(jwt);
                log.debug("JwtRequestFilter: Extracted email from JWT: {}", email);

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = appUserDetialsService.loadUserByUsername(email);

                    if (jwtUtil.validateToken(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authenticationToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );
                        authenticationToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                        log.debug("JwtRequestFilter: User authenticated successfully: {}", email);
                    } else {
                        log.warn("JwtRequestFilter: JWT validation failed for user: {}", email);
                    }
                }
            } catch (Exception e) {
                log.error("JwtRequestFilter: JWT validation error: {}", e.getMessage());
                // Don't throw exception, just continue without authentication
            }
        } else {
            log.debug("JwtRequestFilter: No JWT token found for protected endpoint: {}", servletPath);
        }

        filterChain.doFilter(request, response);
    }
}