package com.example.producer.controller;

import com.example.producer.dto.AuthRequestDto;
import com.example.producer.dto.AuthResponseDto;
import com.example.producer.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    /**
     * POST /api/v1/auth/login
     * Body: { "username": "admin", "password": "admin" }
     * Returns a Bearer JWT token.
     */
    @PostMapping("/login")
    ResponseEntity<AuthResponseDto> login(@RequestBody AuthRequestDto request) {
        log.debug("POST /api/v1/auth/login — username='{}'", request.username());
        AuthResponseDto response = authService.login(request);
        log.debug("POST /api/v1/auth/login — responded 200 OK for username='{}'", request.username());
        return ResponseEntity.ok(response);
    }
}
