package com.example.producer.dto;

import com.example.producer.entity.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MessageRequestDto(

        @NotBlank(message = "origin is required")
        String origin,

        @NotBlank(message = "destination is required")
        String destination,

        @NotNull(message = "messageType is required")
        MessageType messageType,

        String content
) {
}
