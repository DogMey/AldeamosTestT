package com.example.producer.controller;

import com.example.producer.dto.AuthRequestDto;
import com.example.producer.dto.AuthResponseDto;
import com.example.producer.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /auth/login
     * Body: { "username": "admin", "password": "admin" }
     * Returns a Bearer JWT token.
     */
    @PostMapping("/login")
    ResponseEntity<AuthResponseDto> login(@RequestBody AuthRequestDto request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
