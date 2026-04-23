package com.library.catalog.interfaces.rest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record RegisterBookRequest(
        @NotBlank String title,
        @NotBlank String authorFirstName,
        @NotBlank String authorLastName,
        @NotBlank String category,
        @NotBlank String isbn,
        @Min(1) int totalCopies
) {
}
