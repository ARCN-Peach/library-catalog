package com.library.catalog.infrastructure.messaging.consumer;

import java.time.Instant;
import java.util.UUID;

public record BookLentMessage(
        UUID eventId,
        UUID bookId,
        UUID userId,
        Instant dueDate,
        Instant occurredAt,
        String correlationId
) {
}
