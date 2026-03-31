package com.example.producer.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MessageRequestDto {

    @NotBlank(message = "origin is required")
    private String origin;

    @NotBlank(message = "destination is required")
    private String destination;

    @NotBlank(message = "messageType is required")
    private String messageType;

    private String content;
}
