package com.library.catalog.domain.service;

import com.library.catalog.domain.model.SearchCriteria;
import com.library.catalog.domain.model.SearchResult;
import com.library.catalog.domain.repository.BookRepository;

public class CatalogSearchService {

    private final BookRepository bookRepository;

    public CatalogSearchService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public SearchResult search(SearchCriteria criteria, int page, int pageSize) {
        if (page < 0) {
            throw new IllegalArgumentException("Page must be non-negative");
        }
        if (pageSize < 1 || pageSize > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
        return bookRepository.search(normalize(criteria), page, pageSize);
    }

    private SearchCriteria normalize(SearchCriteria criteria) {
        return new SearchCriteria(
                criteria.title() != null ? criteria.title().trim().toLowerCase() : null,
                criteria.author() != null ? criteria.author().trim().toLowerCase() : null,
                criteria.category()
        );
    }
}
