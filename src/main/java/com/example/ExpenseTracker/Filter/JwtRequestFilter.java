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

    private static final List<String> PUBLIC_URLS = Arrays.asList(
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
        "/test-email"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String servletPath = request.getServletPath();
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();

        // ✅ CRITICAL: Log at INFO level so you can always see what path arrives
        log.info("JwtFilter => method={} contextPath='{}' servletPath='{}' requestURI='{}'",
                request.getMethod(), contextPath, servletPath, requestURI);

        // ✅ Check both servletPath AND a stripped version of requestURI as fallback
        boolean isPublic = PUBLIC_URLS.contains(servletPath);

        // Fallback: strip context path from requestURI and check again
        if (!isPublic && requestURI != null) {
            String strippedPath = requestURI.replace(contextPath, "");
            isPublic = PUBLIC_URLS.contains(strippedPath);
            if (isPublic) {
                log.info("JwtFilter => Public URL matched via stripped requestURI: '{}'", strippedPath);
            }
        }

        if (isPublic) {
            log.info("JwtFilter => PUBLIC endpoint, skipping JWT check: '{}'", servletPath);
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = null;
        String email = null;

        // 1. Check Authorization header first
        final String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            log.debug("JwtFilter => JWT found in Authorization header");
        }

        // 2. If not in header, check cookies
        if (jwt == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("jwt".equals(cookie.getName())) {
                        jwt = cookie.getValue();
                        log.debug("JwtFilter => JWT found in cookie");
                        break;
                    }
                }
            }
        }

        // 3. Validate token and set authentication
        if (jwt != null) {
            try {
                email = jwtUtil.extractEmail(jwt);
                log.debug("JwtFilter => Extracted email: {}", email);

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = appUserDetialsService.loadUserByUsername(email);

                    if (jwtUtil.validateToken(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authenticationToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities()
                                );
                        authenticationToken.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                        log.debug("JwtFilter => Authenticated: {}", email);
                    } else {
                        log.warn("JwtFilter => JWT validation failed for: {}", email);
                    }
                }
            } catch (Exception e) {
                log.error("JwtFilter => JWT error: {}", e.getMessage());
            }
        } else {
            log.warn("JwtFilter => No JWT found for PROTECTED path: '{}'", servletPath);
        }

        filterChain.doFilter(request, response);
    }
}