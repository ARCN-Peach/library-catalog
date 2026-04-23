package com.library.catalog.application.dto;

public record SearchBooksQuery(
        String title,
        String author,
        String category,
        int page,
        int pageSize
) {
}
