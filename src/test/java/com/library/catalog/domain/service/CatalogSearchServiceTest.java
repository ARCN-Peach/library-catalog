package com.library.catalog.domain.service;

import com.library.catalog.domain.model.*;
import com.library.catalog.domain.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

class CatalogSearchServiceTest {

    private CapturingBookRepository bookRepository;
    private CatalogSearchService service;

    @BeforeEach
    void setUp() {
        bookRepository = new CapturingBookRepository();
        service = new CatalogSearchService(bookRepository);
    }

    @Test
    void search_normalizes_title_to_lowercase() {
        service.search(new SearchCriteria("Clean Code", null, null), 0, 10);
        assertThat(bookRepository.lastCriteria.title()).isEqualTo("clean code");
    }

    @Test
    void search_trims_title_whitespace() {
        service.search(new SearchCriteria("  Clean Code  ", null, null), 0, 10);
        assertThat(bookRepository.lastCriteria.title()).isEqualTo("clean code");
    }

    @Test
    void search_normalizes_author_to_lowercase() {
        service.search(new SearchCriteria(null, "MARTIN", null), 0, 10);
        assertThat(bookRepository.lastCriteria.author()).isEqualTo("martin");
    }

    @Test
    void search_preserves_null_title() {
        service.search(new SearchCriteria(null, null, null), 0, 10);
        assertThat(bookRepository.lastCriteria.title()).isNull();
    }

    @Test
    void search_preserves_null_author() {
        service.search(new SearchCriteria(null, null, null), 0, 10);
        assertThat(bookRepository.lastCriteria.author()).isNull();
    }

    @Test
    void search_throws_when_page_is_negative() {
        assertThatThrownBy(() -> service.search(SearchCriteria.empty(), -1, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Page must be non-negative");
    }

    @Test
    void search_throws_when_pageSize_is_zero() {
        assertThatThrownBy(() -> service.search(SearchCriteria.empty(), 0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Page size must be between");
    }

    @Test
    void search_throws_when_pageSize_exceeds_100() {
        assertThatThrownBy(() -> service.search(SearchCriteria.empty(), 0, 101))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Page size must be between");
    }

    @Test
    void search_accepts_pageSize_of_100() {
        assertThatCode(() -> service.search(SearchCriteria.empty(), 0, 100))
                .doesNotThrowAnyException();
    }

    static class CapturingBookRepository implements BookRepository {
        SearchCriteria lastCriteria;

        @Override
        public void save(Book book) {}

        @Override
        public Optional<Book> findById(BookId id) { return Optional.empty(); }

        @Override
        public SearchResult search(SearchCriteria criteria, int page, int pageSize) {
            this.lastCriteria = criteria;
            return new SearchResult(List.of(), 0, page, pageSize);
        }
    }
}
