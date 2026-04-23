package com.library.catalog.domain.event;

import java.time.Instant;
import java.util.UUID;

public record BookRegisteredEvent(
        UUID eventId,
        UUID bookId,
        String title,
        String authorFirstName,
        String authorLastName,
        String category,
        String isbn,
        int totalCopies,
        Instant occurredAt,
        String correlationId
) implements DomainEvent {

    @Override
    public String eventType() {
        return "catalog.book.registered.v1";
    }
}
