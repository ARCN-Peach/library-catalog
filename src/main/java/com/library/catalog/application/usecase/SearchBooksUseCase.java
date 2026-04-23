package com.library.catalog.application.usecase;

import com.library.catalog.application.dto.BookSummary;
import com.library.catalog.application.dto.SearchBooksQuery;
import com.library.catalog.application.dto.SearchBooksResult;
import com.library.catalog.domain.model.Book;
import com.library.catalog.domain.model.Category;
import com.library.catalog.domain.model.SearchCriteria;
import com.library.catalog.domain.model.SearchResult;
import com.library.catalog.domain.service.CatalogSearchService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SearchBooksUseCase {

    private final CatalogSearchService catalogSearchService;

    public SearchBooksUseCase(CatalogSearchService catalogSearchService) {
        this.catalogSearchService = catalogSearchService;
    }

    @Transactional(readOnly = true)
    public SearchBooksResult execute(SearchBooksQuery query) {
        Category category = resolveCategory(query.category());
        SearchCriteria criteria = new SearchCriteria(query.title(), query.author(), category);
        SearchResult result = catalogSearchService.search(criteria, query.page(), query.pageSize());

        List<BookSummary> summaries = result.books().stream()
                .map(this::toSummary)
                .toList();

        return new SearchBooksResult(summaries, result.totalFound(), result.page(), result.pageSize());
    }

    private Category resolveCategory(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return Category.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown category: " + raw);
        }
    }

    private BookSummary toSummary(Book book) {
        return new BookSummary(
                book.getId().value(),
                book.getTitle().value(),
                book.getAuthor().firstName(),
                book.getAuthor().lastName(),
                book.getCategory().name(),
                book.getIsbn().value(),
                book.getStatus().name(),
                book.getTotalCopies(),
                book.getAvailableStock()
        );
    }
}
