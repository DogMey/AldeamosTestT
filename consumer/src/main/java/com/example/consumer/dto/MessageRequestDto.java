package com.example.consumer.dto;

public record MessageRequestDto(
        String origin,
        String destination,
        String messageType,
        String content) {}
