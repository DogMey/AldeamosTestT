package com.example.consumer.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;

@Document(collection = "messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDocument {

    @Id
    private String id;

    private String origin;

    @Indexed
    private String destination;

    private String messageType;

    private String content;

    /** Tiempo transcurrido desde que el producer recibió la petición hasta que el consumer persistió el mensaje (ms). */
    private Long processingTime;

    private Instant createdDate;

    /** Populated when a business rule is violated (e.g. destination limit exceeded). */
    private String error;
}
