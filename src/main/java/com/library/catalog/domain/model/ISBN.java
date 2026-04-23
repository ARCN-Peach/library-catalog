package com.library.catalog.domain.model;

public record ISBN(String value) {

    public ISBN {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ISBN cannot be blank");
        }
        String normalized = value.replaceAll("[\\-\\s]", "");
        if (!normalized.matches("\\d{10}|\\d{13}")) {
            throw new IllegalArgumentException("ISBN must be 10 or 13 digits: " + value);
        }
        value = normalized;
    }
}
