package com.example.producer.service;

import com.example.producer.config.RabbitMQTopologyProperties;
import com.example.producer.dto.MessageRequestDto;
import com.example.producer.exception.UnauthorizedOriginException;
import com.example.producer.repository.OriginLineRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);

    private final OriginLineRepository originLineRepository;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQTopologyProperties topology;

    public void processMessage(MessageRequestDto dto) {
        log.info("Processing message request — origin='{}', destination='{}', messageType='{}'",
                dto.origin(), dto.destination(), dto.messageType());

        try {
            boolean authorized = originLineRepository.existsByOrigin(dto.origin());
            if (!authorized) {
                log.warn("Rejected message: origin '{}' is not an authorized line", dto.origin());
                throw new UnauthorizedOriginException(
                        "Origin '" + dto.origin() + "' is not an authorized line");
            }
        } catch (UnauthorizedOriginException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Error querying origin lines for origin '{}': {}", dto.origin(), ex.getMessage(), ex);
            throw ex;
        }

        try {
            rabbitTemplate.convertAndSend(
                    topology.getExchange(),
                    topology.getRoutingKey(),
                    dto,
                    message -> {
                        long timestamp = System.currentTimeMillis();
                        message.getMessageProperties().setHeader("x-timestamp-reception", timestamp);
                        log.debug("Set RabbitMQ header x-timestamp-reception={} for destination='{}'",
                                timestamp, dto.destination());
                        return message;
                    });

            log.info("Message published to exchange='{}' routingKey='{}' — origin='{}', destination='{}'",
                    topology.getExchange(), topology.getRoutingKey(), dto.origin(), dto.destination());

        } catch (AmqpException ex) {
            log.error("Failed to publish message to RabbitMQ — origin='{}', destination='{}': {}",
                    dto.origin(), dto.destination(), ex.getMessage(), ex);
            throw ex;
        }
    }
}
