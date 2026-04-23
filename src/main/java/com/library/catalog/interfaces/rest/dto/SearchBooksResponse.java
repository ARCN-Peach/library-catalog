package com.library.catalog.interfaces.rest.dto;

import java.util.List;

public record SearchBooksResponse(
        List<BookResponse> books,
        long totalFound,
        int page,
        int pageSize
) {
}
