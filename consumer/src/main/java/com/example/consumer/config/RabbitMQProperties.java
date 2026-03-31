package com.example.consumer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rabbitmq")
public record RabbitMQProperties(String exchange, String routingKey, String queue) {}
