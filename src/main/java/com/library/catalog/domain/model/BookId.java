package com.library.catalog.domain.model;

import java.util.UUID;

public record BookId(UUID value) {

    public BookId {
        if (value == null) {
            throw new IllegalArgumentException("BookId cannot be null");
        }
    }

    public static BookId generate() {
        return new BookId(UUID.randomUUID());
    }

    public static BookId of(UUID value) {
        return new BookId(value);
    }

    public static BookId of(String value) {
        return new BookId(UUID.fromString(value));
    }
}
