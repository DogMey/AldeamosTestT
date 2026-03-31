package com.example.consumer.listener;

import com.example.consumer.dto.MessageRequestDto;
import com.example.consumer.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class MessageListener {

    private static final Logger log = LoggerFactory.getLogger(MessageListener.class);

    private final MessageService messageService;

    @RabbitListener(queues = "${rabbitmq.queue}")
    void receive(MessageRequestDto dto, @Headers Map<String, Object> headers) {
        log.info("Message received — origin={}, destination={}, type={}",
                dto.origin(), dto.destination(), dto.messageType());

        long receptionTimestamp = parseTimestamp(headers.get("x-timestamp-reception"));
        try {
            messageService.processMessage(dto, receptionTimestamp);
        } catch (Exception ex) {
            log.error("Unexpected error processing message from origin={}: {}",
                    dto.origin(), ex.getMessage(), ex);
            throw ex;
        }
    }

    private long parseTimestamp(Object value) {
        if (value instanceof Long l) {
            return l;
        }
        if (value != null) {
            try {
                return Long.parseLong(value.toString());
            } catch (NumberFormatException ex) {
                log.warn("Could not parse x-timestamp-reception header value='{}': {}", value, ex.getMessage());
            }
        }
        return 0L;
    }
}
