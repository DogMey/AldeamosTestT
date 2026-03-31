package com.example.producer.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;

    /** Skip JWT processing for public endpoints */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        boolean skip = request.getServletPath().startsWith("/auth");
        if (skip) {
            log.debug("Skipping JWT filter for public path: {}", request.getServletPath());
        }
        return skip;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No Bearer token found in request to '{}', continuing filter chain",
                    request.getServletPath());
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            if (jwtService.isTokenValid(token)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                String username = jwtService.extractUsername(token);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("JWT authenticated — username='{}', path='{}'", username, request.getServletPath());

            } else if (!jwtService.isTokenValid(token)) {
                log.warn("Invalid JWT received for path='{}'", request.getServletPath());
            }
        } catch (Exception ex) {
            log.error("Unexpected error processing JWT for path='{}': {}",
                    request.getServletPath(), ex.getMessage(), ex);
        }

        filterChain.doFilter(request, response);
    }
}
