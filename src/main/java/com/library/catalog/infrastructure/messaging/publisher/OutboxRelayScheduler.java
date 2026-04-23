package com.library.catalog.infrastructure.messaging.publisher;

import com.library.catalog.infrastructure.persistence.outbox.OutboxEventEntity;
import com.library.catalog.infrastructure.persistence.outbox.OutboxJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

@Component
public class OutboxRelayScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelayScheduler.class);
    private static final String CATALOG_EXCHANGE = "catalog";

    private final OutboxJpaRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;

    public OutboxRelayScheduler(OutboxJpaRepository outboxRepository, RabbitTemplate rabbitTemplate) {
        this.outboxRepository = outboxRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedDelayString = "${catalog.outbox.relay-delay-ms:5000}")
    @Transactional
    public void relayPendingEvents() {
        List<OutboxEventEntity> pending = outboxRepository.findByPublishedFalseOrderByOccurredAtAsc();
        for (OutboxEventEntity event : pending) {
            try {
                Message message = MessageBuilder
                        .withBody(event.getPayload().getBytes(StandardCharsets.UTF_8))
                        .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                        .setCorrelationId(event.getCorrelationId())
                        .setHeader("eventType", event.getEventType())
                        .build();

                rabbitTemplate.send(CATALOG_EXCHANGE, event.getEventType(), message);

                event.setPublished(true);
                event.setPublishedAt(Instant.now());
                outboxRepository.save(event);

                log.info("Relayed event {} [id={}]", event.getEventType(), event.getId());
            } catch (Exception e) {
                log.error("Failed to relay outbox event {} [id={}]: {}",
                        event.getEventType(), event.getId(), e.getMessage());
            }
        }
    }
}
