package com.example.producer.security;

import com.example.producer.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final JwtProperties jwtProperties;

    public String generateToken(String username) {
        try {
            String token = Jwts.builder()
                    .subject(username)
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + jwtProperties.getExpirationMs()))
                    .signWith(signingKey())
                    .compact();
            log.debug("JWT generated for username='{}'", username);
            return token;
        } catch (Exception ex) {
            log.error("Error generating JWT for username='{}': {}", username, ex.getMessage(), ex);
            throw ex;
        }
    }

    public String extractUsername(String token) {
        try {
            String username = parseClaims(token).getSubject();
            log.debug("Extracted username='{}' from JWT", username);
            return username;
        } catch (Exception ex) {
            log.warn("Could not extract username from JWT: {}", ex.getMessage());
            throw ex;
        }
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            log.debug("JWT validation passed");
            return true;
        } catch (JwtException ex) {
            log.warn("JWT validation failed (JwtException): {}", ex.getMessage());
            return false;
        } catch (IllegalArgumentException ex) {
            log.warn("JWT validation failed (invalid argument): {}", ex.getMessage());
            return false;
        }
    }

    public long getExpirationMs() {
        return jwtProperties.getExpirationMs();
    }

    // ── private ──────────────────────────────────────────────────────────────

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}
