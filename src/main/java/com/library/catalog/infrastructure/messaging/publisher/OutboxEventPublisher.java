package com.library.catalog.infrastructure.messaging.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.catalog.application.port.DomainEventPublisher;
import com.library.catalog.domain.event.DomainEvent;
import com.library.catalog.infrastructure.persistence.outbox.OutboxEventEntity;
import com.library.catalog.infrastructure.persistence.outbox.OutboxJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OutboxEventPublisher implements DomainEventPublisher {

    private final OutboxJpaRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OutboxEventPublisher(OutboxJpaRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(List<DomainEvent> events) {
        for (DomainEvent event : events) {
            try {
                OutboxEventEntity entity = new OutboxEventEntity();
                entity.setId(event.eventId());
                entity.setEventType(event.eventType());
                entity.setPayload(objectMapper.writeValueAsString(event));
                entity.setOccurredAt(event.occurredAt());
                entity.setCorrelationId(event.correlationId());
                entity.setPublished(false);
                outboxRepository.save(entity);
            } catch (Exception e) {
                throw new RuntimeException("Failed to store domain event in outbox: " + event.eventType(), e);
            }
        }
    }
}
