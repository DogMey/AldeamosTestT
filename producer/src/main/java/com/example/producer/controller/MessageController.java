package com.example.producer.controller;

import com.example.producer.dto.MessageRequestDto;
import com.example.producer.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private static final Logger log = LoggerFactory.getLogger(MessageController.class);

    private final MessageService messageService;

    /**
     * POST /messages/send
     * Requires: Authorization: Bearer &lt;token&gt;
     * Body: { "origin": "...", "destination": "...", "messageType": "...", "content": "..." }
     */
    @PostMapping("/send")
    ResponseEntity<Map<String, String>> send(@Valid @RequestBody MessageRequestDto dto) {
        log.debug("POST /messages/send — origin='{}', destination='{}', messageType='{}'",
                dto.origin(), dto.destination(), dto.messageType());

        messageService.processMessage(dto);

        log.info("POST /messages/send — 200 OK for origin='{}', destination='{}'",
                dto.origin(), dto.destination());

        return ResponseEntity.ok(Map.of("status", "Message sent successfully"));
    }
}
