package com.library.catalog.infrastructure.messaging.consumer;

import java.time.Instant;
import java.util.UUID;

// Matches the DomainEventMessage envelope that library-rental publishes:
// { "type": "...", "occurredAt": "...", "payload": { "rentalId", "userId", "bookId", "returnedAt" } }
public record BookReturnedMessage(String type, Instant occurredAt, Payload payload) {

    public record Payload(UUID rentalId, UUID userId, UUID bookId, Instant returnedAt) {}
}
