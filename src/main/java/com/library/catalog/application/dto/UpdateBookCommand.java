package com.library.catalog.application.dto;

import java.util.UUID;

public record UpdateBookCommand(
        UUID bookId,
        String title,
        String authorFirstName,
        String authorLastName,
        String category,
        String isbn,
        String correlationId
) {
}
