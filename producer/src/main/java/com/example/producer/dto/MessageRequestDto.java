package com.example.producer.dto;

import com.example.producer.entity.MessageType;
import com.example.producer.validation.ValidMessageContent;
import com.example.producer.validation.ValidPhoneNumber;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@ValidMessageContent
public record MessageRequestDto(

        @NotBlank(message = "origin is required")
        @ValidPhoneNumber
        String origin,

        @NotBlank(message = "destination is required")
        @ValidPhoneNumber
        String destination,

        @NotNull(message = "messageType is required — valid values: TEXTO, IMAGEN, VIDEO, DOCUMENTO")
        MessageType messageType,

        @NotBlank(message = "content is required")
        String content
) {
}
