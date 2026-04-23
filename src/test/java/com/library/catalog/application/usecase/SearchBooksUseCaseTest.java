package com.library.catalog.application.usecase;

import com.library.catalog.application.dto.SearchBooksQuery;
import com.library.catalog.application.dto.SearchBooksResult;
import com.library.catalog.domain.model.*;
import com.library.catalog.domain.repository.BookRepository;
import com.library.catalog.domain.service.CatalogSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

class SearchBooksUseCaseTest {

    private InMemoryBookRepository bookRepository;
    private SearchBooksUseCase useCase;

    @BeforeEach
    void setUp() {
        bookRepository = new InMemoryBookRepository();
        CatalogSearchService searchService = new CatalogSearchService(bookRepository);
        useCase = new SearchBooksUseCase(searchService);
    }

    @Test
    void search_without_criteria_returns_all_published_books() {
        bookRepository.save(aBook("Clean Code", "Robert", "Martin", Category.TECHNOLOGY));
        bookRepository.save(aBook("Sapiens", "Yuval", "Harari", Category.HISTORY));

        SearchBooksResult result = useCase.execute(new SearchBooksQuery(null, null, null, 0, 20));

        assertThat(result.totalFound()).isEqualTo(2);
        assertThat(result.books()).hasSize(2);
    }

    @Test
    void search_by_title_returns_matching_books() {
        bookRepository.save(aBook("Clean Code", "Robert", "Martin", Category.TECHNOLOGY));
        bookRepository.save(aBook("Clean Architecture", "Robert", "Martin", Category.TECHNOLOGY));
        bookRepository.save(aBook("Sapiens", "Yuval", "Harari", Category.HISTORY));

        SearchBooksResult result = useCase.execute(new SearchBooksQuery("clean", null, null, 0, 20));

        assertThat(result.totalFound()).isEqualTo(2);
        assertThat(result.books()).allMatch(b -> b.title().toLowerCase().contains("clean"));
    }

    @Test
    void search_by_category_filters_correctly() {
        bookRepository.save(aBook("Clean Code", "Robert", "Martin", Category.TECHNOLOGY));
        bookRepository.save(aBook("Sapiens", "Yuval", "Harari", Category.HISTORY));

        SearchBooksResult result = useCase.execute(
                new SearchBooksQuery(null, null, "HISTORY", 0, 20));

        assertThat(result.totalFound()).isEqualTo(1);
        assertThat(result.books().get(0).title()).isEqualTo("Sapiens");
    }

    @Test
    void search_by_author_is_case_insensitive() {
        bookRepository.save(aBook("Clean Code", "Robert", "Martin", Category.TECHNOLOGY));

        SearchBooksResult result = useCase.execute(
                new SearchBooksQuery(null, "MARTIN", null, 0, 20));

        assertThat(result.totalFound()).isEqualTo(1);
    }

    @Test
    void search_returns_empty_when_no_match() {
        bookRepository.save(aBook("Clean Code", "Robert", "Martin", Category.TECHNOLOGY));

        SearchBooksResult result = useCase.execute(
                new SearchBooksQuery("nonexistent", null, null, 0, 20));

        assertThat(result.totalFound()).isEqualTo(0);
        assertThat(result.books()).isEmpty();
    }

    @Test
    void search_excludes_retired_books() {
        Book retired = aBook("Old Book", "A", "B", Category.FICTION);
        retired.retire("corr");
        bookRepository.save(retired);

        SearchBooksResult result = useCase.execute(
                new SearchBooksQuery(null, null, null, 0, 20));

        assertThat(result.totalFound()).isEqualTo(0);
    }

    @Test
    void search_rejects_invalid_pageSize() {
        assertThatThrownBy(() -> useCase.execute(
                new SearchBooksQuery(null, null, null, 0, 200)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private Book aBook(String title, String firstName, String lastName, Category category) {
        return Book.register(
                new Title(title),
                new Author(firstName, lastName),
                category,
                new ISBN("9780132350884"),
                1,
                "test-corr"
        );
    }

    // ── In-memory repository ─────────────────────────────────────────────────

    static class InMemoryBookRepository implements BookRepository {
        private final Map<BookId, Book> store = new HashMap<>();

        @Override
        public void save(Book book) {
            store.put(book.getId(), book);
        }

        @Override
        public Optional<Book> findById(BookId id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public SearchResult search(SearchCriteria criteria, int page, int pageSize) {
            List<Book> filtered = store.values().stream()
                    .filter(b -> b.getStatus() == BookStatus.PUBLISHED)
                    .filter(b -> criteria.title() == null ||
                            b.getTitle().value().toLowerCase().contains(criteria.title()))
                    .filter(b -> criteria.author() == null ||
                            b.getAuthor().fullName().toLowerCase().contains(criteria.author()))
                    .filter(b -> criteria.category() == null ||
                            b.getCategory() == criteria.category())
                    .toList();

            int from = page * pageSize;
            int to = Math.min(from + pageSize, filtered.size());
            List<Book> pageBooks = from >= filtered.size() ? List.of() : filtered.subList(from, to);
            return new SearchResult(pageBooks, filtered.size(), page, pageSize);
        }
    }
}
