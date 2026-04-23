package com.library.catalog.application.dto;

import java.util.UUID;

public record BookSummary(
        UUID id,
        String title,
        String authorFirstName,
        String authorLastName,
        String category,
        String isbn,
        String status,
        int totalCopies,
        int availableStock
) {
}
