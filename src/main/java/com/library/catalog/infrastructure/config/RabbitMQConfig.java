package com.library.catalog.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ── Catalog exchange (producer) ──────────────────────────────────────────
    @Bean
    public TopicExchange catalogExchange() {
        return ExchangeBuilder.topicExchange("catalog").durable(true).build();
    }

    // ── Rental exchange (consumer) ───────────────────────────────────────────
    @Bean
    public TopicExchange rentalExchange() {
        return ExchangeBuilder.topicExchange("rental").durable(true).build();
    }

    // ── Queues ───────────────────────────────────────────────────────────────
    @Bean
    public Queue bookLentQueue() {
        return QueueBuilder.durable("catalog.rental.book-lent")
                .withArgument("x-dead-letter-exchange", "catalog.dlq")
                .build();
    }

    @Bean
    public Queue bookReturnedQueue() {
        return QueueBuilder.durable("catalog.rental.book-returned")
                .withArgument("x-dead-letter-exchange", "catalog.dlq")
                .build();
    }

    @Bean
    public Binding bookLentBinding(Queue bookLentQueue, TopicExchange rentalExchange) {
        return BindingBuilder.bind(bookLentQueue).to(rentalExchange).with("rental.book.lent.v1");
    }

    @Bean
    public Binding bookReturnedBinding(Queue bookReturnedQueue, TopicExchange rentalExchange) {
        return BindingBuilder.bind(bookReturnedQueue).to(rentalExchange).with("rental.book.returned.v1");
    }

    // ── Dead-letter queues ───────────────────────────────────────────────────
    @Bean
    public TopicExchange catalogDlqExchange() {
        return ExchangeBuilder.topicExchange("catalog.dlq").durable(true).build();
    }

    @Bean
    public Queue bookLentDlq() {
        return QueueBuilder.durable("catalog.rental.book-lent.dlq").build();
    }

    @Bean
    public Queue bookReturnedDlq() {
        return QueueBuilder.durable("catalog.rental.book-returned.dlq").build();
    }

    // ── Converters ───────────────────────────────────────────────────────────
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        return factory;
    }
}
