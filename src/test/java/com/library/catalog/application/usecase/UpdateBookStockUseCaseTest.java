package com.library.catalog.application.usecase;

import com.library.catalog.domain.exception.BookNotFoundException;
import com.library.catalog.domain.model.*;
import com.library.catalog.domain.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

class UpdateBookStockUseCaseTest {

    private InMemoryBookRepository bookRepository;
    private UpdateBookStockUseCase useCase;

    @BeforeEach
    void setUp() {
        bookRepository = new InMemoryBookRepository();
        useCase = new UpdateBookStockUseCase(bookRepository);
    }

    @Test
    void decrementStock_reduces_available_stock() {
        Book book = savedBook(3);

        useCase.decrementStock(book.getId().value(), "corr");

        assertThat(bookRepository.findById(book.getId()).get().getAvailableStock()).isEqualTo(2);
    }

    @Test
    void decrementStock_does_not_throw_on_insufficient_stock() {
        Book book = savedBook(1);
        book.decrementStock();
        bookRepository.save(book);

        assertThatCode(() -> useCase.decrementStock(book.getId().value(), "corr"))
                .doesNotThrowAnyException();
    }

    @Test
    void decrementStock_throws_when_book_not_found() {
        assertThatThrownBy(() -> useCase.decrementStock(UUID.randomUUID(), "corr"))
                .isInstanceOf(BookNotFoundException.class);
    }

    @Test
    void incrementStock_increases_available_stock() {
        Book book = savedBook(3);
        book.decrementStock();
        bookRepository.save(book);

        useCase.incrementStock(book.getId().value(), "corr");

        assertThat(bookRepository.findById(book.getId()).get().getAvailableStock()).isEqualTo(3);
    }

    @Test
    void incrementStock_throws_when_book_not_found() {
        assertThatThrownBy(() -> useCase.incrementStock(UUID.randomUUID(), "corr"))
                .isInstanceOf(BookNotFoundException.class);
    }

    private Book savedBook(int copies) {
        Book book = Book.register(
                new Title("Test Book"), new Author("A", "B"),
                Category.FICTION, new ISBN("9780132350884"), copies, "corr"
        );
        book.pullDomainEvents();
        bookRepository.save(book);
        return book;
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
