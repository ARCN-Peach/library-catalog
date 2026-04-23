package com.library.catalog.infrastructure.messaging.consumer;

import java.time.Instant;
import java.util.UUID;

public record BookReturnedMessage(
        UUID eventId,
        UUID bookId,
        UUID userId,
        Instant returnDate,
        Instant occurredAt,
        String correlationId
) {
}
