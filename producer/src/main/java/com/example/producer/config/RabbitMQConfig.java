package com.example.producer.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    private final RabbitMQTopologyProperties topology;

    public RabbitMQConfig(RabbitMQTopologyProperties topology) {
        this.topology = topology;
    }

    @Bean
    TopicExchange producerExchange() {
        return new TopicExchange(topology.getExchange());
    }

    @Bean
    Queue producerQueue() {
        return QueueBuilder.durable(topology.getQueue()).build();
    }

    @Bean
    Binding producerBinding(Queue producerQueue, TopicExchange producerExchange) {
        return BindingBuilder.bind(producerQueue)
                .to(producerExchange)
                .with(topology.getRoutingKey());
    }

    @Bean
    MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
