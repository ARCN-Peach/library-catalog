package com.library.catalog.application.usecase;

import com.library.catalog.application.dto.BookSummary;
import com.library.catalog.domain.exception.BookNotFoundException;
import com.library.catalog.domain.model.*;
import com.library.catalog.domain.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

class GetBookUseCaseTest {

    private InMemoryBookRepository bookRepository;
    private GetBookUseCase useCase;

    @BeforeEach
    void setUp() {
        bookRepository = new InMemoryBookRepository();
        useCase = new GetBookUseCase(bookRepository);
    }

    @Test
    void execute_returns_summary_for_existing_book() {
        Book book = Book.register(
                new Title("Clean Code"), new Author("Robert", "Martin"),
                Category.TECHNOLOGY, new ISBN("9780132350884"), 3, "corr"
        );
        bookRepository.save(book);

        BookSummary result = useCase.execute(book.getId().value());

        assertThat(result.title()).isEqualTo("Clean Code");
        assertThat(result.authorFirstName()).isEqualTo("Robert");
        assertThat(result.authorLastName()).isEqualTo("Martin");
        assertThat(result.category()).isEqualTo("TECHNOLOGY");
        assertThat(result.isbn()).isEqualTo("9780132350884");
        assertThat(result.status()).isEqualTo("PUBLISHED");
        assertThat(result.totalCopies()).isEqualTo(3);
        assertThat(result.availableStock()).isEqualTo(3);
    }

    @Test
    void execute_returns_id_matching_book() {
        Book book = Book.register(
                new Title("DDD"), new Author("Eric", "Evans"),
                Category.TECHNOLOGY, new ISBN("9780321125217"), 1, "corr"
        );
        bookRepository.save(book);

        BookSummary result = useCase.execute(book.getId().value());

        assertThat(result.id()).isEqualTo(book.getId().value());
    }

    @Test
    void execute_throws_when_book_not_found() {
        assertThatThrownBy(() -> useCase.execute(UUID.randomUUID()))
                .isInstanceOf(BookNotFoundException.class);
    }

    static class InMemoryBookRepository implements BookRepository {
        private final Map<BookId, Book> store = new HashMap<>();

        @Override
        public void save(Book book) { store.put(book.getId(), book); }

        @Override
        public Optional<Book> findById(BookId id) { return Optional.ofNullable(store.get(id)); }

        @Override
        public SearchResult search(SearchCriteria c, int page, int pageSize) {
            return new SearchResult(List.of(), 0, page, pageSize);
        }
    }
}
