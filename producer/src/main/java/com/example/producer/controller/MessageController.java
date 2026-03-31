package com.example.producer.controller;

import com.example.producer.dto.MessageRequestDto;
import com.example.producer.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /**
     * POST /messages/send
     * Requires: Authorization: Bearer <token>
     * Body: { "origin": "LINE_A", "destination": "...", "messageType": "...",
     * "content": "..." }
     */
    @PostMapping("/send")
    ResponseEntity<Map<String, String>> send(@Valid @RequestBody MessageRequestDto dto) {
        messageService.processMessage(dto);
        return ResponseEntity.ok(Map.of("status", "Message sent successfully"));
    }
}
