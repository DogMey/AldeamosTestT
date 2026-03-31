package com.example.consumer.service;

import com.example.consumer.document.MessageDocument;
import com.example.consumer.dto.MessageRequestDto;

import java.util.List;

public interface MessageService {

    void processMessage(MessageRequestDto dto, long receptionTimestamp);

    List<MessageDocument> findByDestination(String destination);
}
