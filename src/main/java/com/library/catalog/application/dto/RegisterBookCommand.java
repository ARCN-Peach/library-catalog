package com.library.catalog.application.dto;

public record RegisterBookCommand(
        String title,
        String authorFirstName,
        String authorLastName,
        String category,
        String isbn,
        int totalCopies,
        String correlationId
) {
}
