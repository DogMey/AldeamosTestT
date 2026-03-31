package com.example.consumer.controller;

import com.example.consumer.document.MessageDocument;
import com.example.consumer.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping
    ResponseEntity<List<MessageDocument>> getByDestination(@RequestParam String destination) {
        return ResponseEntity.ok(messageService.findByDestination(destination));
    }
}
