package com.library.catalog.domain.model;

import java.util.List;

public record SearchResult(List<Book> books, long totalFound, int page, int pageSize) {
}
