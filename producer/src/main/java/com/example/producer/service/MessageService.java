package com.example.producer.service;

import com.example.producer.dto.MessageRequestDto;

public interface MessageService {

    /**
     * Valida el origin contra las líneas autorizadas en MySQL y,
     * si es válido, publica el mensaje en RabbitMQ con el header de timestamp.
     *
     * @param dto datos del mensaje recibido por el API
     * @throws com.example.producer.exception.UnauthorizedOriginException si el origin no está autorizado
     * @throws org.springframework.amqp.AmqpException si falla la publicación en RabbitMQ
     */
    void processMessage(MessageRequestDto dto);
}
