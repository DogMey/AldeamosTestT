package com.example.producer.service.impl;

import com.example.producer.config.AuthProperties;
import com.example.producer.dto.AuthRequestDto;
import com.example.producer.dto.AuthResponseDto;
import com.example.producer.security.JwtService;
import com.example.producer.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthProperties authProperties;
    private final JwtService jwtService;

    @Override
    public AuthResponseDto login(AuthRequestDto request) {
        log.info("Login attempt for username='{}'", request.username());

        try {
            if (!authProperties.getUsername().equals(request.username())
                    || !authProperties.getPassword().equals(request.password())) {
                log.warn("Failed login attempt for username='{}'", request.username());
                throw new BadCredentialsException("Invalid username or password");
            }

            String token = jwtService.generateToken(request.username());
            long expiresIn = jwtService.getExpirationMs() / 1000;

            log.info("Login successful for username='{}', token expires in {} seconds",
                    request.username(), expiresIn);

            return new AuthResponseDto(token, expiresIn);

        } catch (BadCredentialsException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error during login for username='{}': {}",
                    request.username(), ex.getMessage(), ex);
            throw ex;
        }
    }
}
