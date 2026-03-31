package com.example.producer.service.impl;

import com.example.producer.config.RabbitMQTopologyProperties;
import com.example.producer.dto.MessageRequestDto;
import com.example.producer.exception.UnauthorizedOriginException;
import com.example.producer.repository.OriginLineRepository;
import com.example.producer.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);

    private final OriginLineRepository originLineRepository;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQTopologyProperties topology;

    @Override
    public void processMessage(MessageRequestDto dto) {
        log.info("Processing message — origin='{}', destination='{}', messageType='{}'",
                dto.origin(), dto.destination(), dto.messageType());

        validateOrigin(dto.origin());
        publishToRabbitMQ(dto);
    }

    // ── private ──────────────────────────────────────────────────────────────

    private void validateOrigin(String origin) {
        try {
            if (!originLineRepository.existsByOrigin(origin)) {
                log.warn("Rejected: origin '{}' is not an authorized line", origin);
                throw new UnauthorizedOriginException(
                        "Origin '" + origin + "' is not an authorized line");
            }
            log.debug("Origin '{}' is authorized", origin);
        } catch (UnauthorizedOriginException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error querying origin lines for origin='{}': {}", origin, ex.getMessage(), ex);
            throw ex;
        }
    }

    private void publishToRabbitMQ(MessageRequestDto dto) {
        try {
            rabbitTemplate.convertAndSend(
                    topology.getExchange(),
                    topology.getRoutingKey(),
                    dto,
                    message -> {
                        long timestamp = System.currentTimeMillis();
                        message.getMessageProperties().setHeader("x-timestamp-reception", timestamp);
                        log.debug("Header x-timestamp-reception={} set for destination='{}'",
                                timestamp, dto.destination());
                        return message;
                    });

            log.info("Message published — exchange='{}', routingKey='{}', origin='{}', destination='{}'",
                    topology.getExchange(), topology.getRoutingKey(),
                    dto.origin(), dto.destination());

        } catch (AmqpException ex) {
            log.error("Failed to publish to RabbitMQ — origin='{}', destination='{}': {}",
                    dto.origin(), dto.destination(), ex.getMessage(), ex);
            throw ex;
        }
    }
}
