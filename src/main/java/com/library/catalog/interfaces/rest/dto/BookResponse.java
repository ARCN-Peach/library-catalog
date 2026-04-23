package com.library.catalog.interfaces.rest.dto;

import java.util.UUID;

public record BookResponse(
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
