package com.example.producer.dto;

public record AuthResponseDto(String accessToken, String tokenType, long expiresIn) {

    /** Convenience constructor: tokenType is always "Bearer" */
    public AuthResponseDto(String accessToken, long expiresIn) {
        this(accessToken, "Bearer", expiresIn);
    }
}
