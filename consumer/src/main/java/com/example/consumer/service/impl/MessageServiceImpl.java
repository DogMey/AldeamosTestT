package com.example.consumer.service.impl;

import com.example.consumer.document.MessageDocument;
import com.example.consumer.dto.MessageRequestDto;
import com.example.consumer.repository.MessageRepository;
import com.example.consumer.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);
    private static final int MAX_MESSAGES_PER_DAY = 3;

    private final MessageRepository messageRepository;

    @Override
    public void processMessage(MessageRequestDto dto, long receptionTimestamp) {
        Instant now = Instant.now();
        long processingTime = receptionTimestamp > 0
                ? System.currentTimeMillis() - receptionTimestamp
                : 0L;

        MessageDocument.MessageDocumentBuilder builder = MessageDocument.builder()
                .id(UUID.randomUUID().toString())
                .origin(dto.origin())
                .destination(dto.destination())
                .messageType(dto.messageType())
                .content(dto.content())
                .processingTime(processingTime)
                .createdDate(now);

        Instant windowStart = now.minus(24, ChronoUnit.HOURS);
        long count = messageRepository.countByDestinationAndCreatedDateAfterAndErrorIsNull(
                dto.destination(), windowStart);

        if (count >= MAX_MESSAGES_PER_DAY) {
            String error = String.format(
                    "Destination %s has reached the limit of %d messages in 24h",
                    dto.destination(), MAX_MESSAGES_PER_DAY);
            log.warn("Business rule violated: {}", error);
            messageRepository.save(builder.error(error).build());
        } else {
            messageRepository.save(builder.build());
            log.info("Message saved — origin={}, destination={}, messagesLast24h={}",
                    dto.origin(), dto.destination(), count + 1);
        }
    }

    @Override
    public List<MessageDocument> findByDestination(String destination) {
        log.info("Querying messages for destination={}", destination);
        return messageRepository.findByDestination(destination);
    }
}
