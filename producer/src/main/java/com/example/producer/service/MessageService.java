package com.example.producer.service;

import com.example.producer.config.RabbitMQTopologyProperties;
import com.example.producer.dto.MessageRequestDto;
import com.example.producer.exception.UnauthorizedOriginException;
import com.example.producer.repository.OriginLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final OriginLineRepository originLineRepository;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQTopologyProperties topology;

    public void processMessage(MessageRequestDto dto) {
        // Validar origin en MySQL
        if (!originLineRepository.existsByPhoneNumberAndActiveTrue(dto.getOrigin())) {
            throw new UnauthorizedOriginException(
                    "Origin '" + dto.getOrigin() + "' is not an authorized line");
        }

        // Enviar a RabbitMQ con header x-timestamp-reception
        rabbitTemplate.convertAndSend(
                topology.getExchange(),
                topology.getRoutingKey(),
                dto,
                message -> {
                    message.getMessageProperties()
                            .setHeader("x-timestamp-reception", System.currentTimeMillis());
                    return message;
                });
    }
}
