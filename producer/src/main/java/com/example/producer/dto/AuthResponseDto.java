package com.example.producer.dto;

import lombok.Getter;

@Getter
public class AuthResponseDto {
    private final String accessToken;
    private final String tokenType = "Bearer";
    private final long expiresIn;

    public AuthResponseDto(String accessToken, long expiresIn) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
    }
}
