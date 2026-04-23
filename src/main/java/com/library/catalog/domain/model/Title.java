package com.library.catalog.domain.model;

public record Title(String value) {

    public Title {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Title cannot be blank");
        }
        if (value.length() > 255) {
            throw new IllegalArgumentException("Title cannot exceed 255 characters");
        }
        value = value.trim();
    }
}
