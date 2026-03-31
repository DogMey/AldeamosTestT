package com.example.producer.service.impl;

import com.example.producer.config.RabbitMQTopologyProperties;
import com.example.producer.dto.MessageRequestDto;
import com.example.producer.entity.MessageType;
import com.example.producer.exception.UnauthorizedOriginException;
import com.example.producer.repository.OriginLineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageServiceImpl — validación de origin y publicación RabbitMQ")
class MessageServiceImplTest {

    @Mock private OriginLineRepository originLineRepository;
    @Mock private RabbitTemplate rabbitTemplate;
    @Mock private RabbitMQTopologyProperties topology;
    @InjectMocks private MessageServiceImpl messageService;

    private static final String ORIGIN = "+573001234567";

    @BeforeEach
    void setUp() {
        when(topology.getExchange()).thenReturn("producer.exchange");
        when(topology.getRoutingKey()).thenReturn("producer.messages");
    }

    @Test
    @DisplayName("origin autorizado debe publicar el mensaje en RabbitMQ")
    void authorizedOriginShouldPublish() {
        when(originLineRepository.existsByOrigin(ORIGIN)).thenReturn(true);
        var dto = new MessageRequestDto(ORIGIN, "+573009876543", MessageType.TEXTO, "Hola");

        messageService.processMessage(dto);

        verify(rabbitTemplate).convertAndSend(eq("producer.exchange"), eq("producer.messages"),
                eq(dto), any(org.springframework.amqp.core.MessagePostProcessor.class));
    }

    @Test
    @DisplayName("origin no autorizado debe lanzar UnauthorizedOriginException sin publicar")
    void unauthorizedOriginShouldThrowAndNotPublish() {
        when(originLineRepository.existsByOrigin(any())).thenReturn(false);
        var dto = new MessageRequestDto("+570000000000", "+573009876543", MessageType.TEXTO, "Hola");

        assertThatThrownBy(() -> messageService.processMessage(dto))
                .isInstanceOf(UnauthorizedOriginException.class);

        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    @DisplayName("fallo en RabbitMQ debe propagar AmqpException")
    void rabbitMqFailureShouldPropagateException() {
        when(originLineRepository.existsByOrigin(ORIGIN)).thenReturn(true);
        doThrow(new AmqpException("Broker unavailable"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class),
                        any(org.springframework.amqp.core.MessagePostProcessor.class));

        var dto = new MessageRequestDto(ORIGIN, "+573009876543", MessageType.TEXTO, "Hola");

        assertThatThrownBy(() -> messageService.processMessage(dto))
                .isInstanceOf(AmqpException.class);
    }
}
