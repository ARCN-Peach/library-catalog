package com.library.catalog.application.dto;

import java.util.List;

public record SearchBooksResult(
        List<BookSummary> books,
        long totalFound,
        int page,
        int pageSize
) {
}
