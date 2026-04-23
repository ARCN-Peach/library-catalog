package com.library.catalog.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateBookRequest(
        @NotBlank String title,
        @NotBlank String authorFirstName,
        @NotBlank String authorLastName,
        @NotBlank String category,
        @NotBlank String isbn
) {
}
