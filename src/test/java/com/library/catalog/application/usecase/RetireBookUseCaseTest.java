package com.library.catalog.application.usecase;

import com.library.catalog.application.port.DomainEventPublisher;
import com.library.catalog.domain.event.BookRetiredEvent;
import com.library.catalog.domain.event.DomainEvent;
import com.library.catalog.domain.exception.BookAlreadyRetiredException;
import com.library.catalog.domain.exception.BookNotFoundException;
import com.library.catalog.domain.model.*;
import com.library.catalog.domain.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

class RetireBookUseCaseTest {

    private InMemoryBookRepository bookRepository;
    private CapturingEventPublisher eventPublisher;
    private RetireBookUseCase useCase;

    @BeforeEach
    void setUp() {
        bookRepository = new InMemoryBookRepository();
        eventPublisher = new CapturingEventPublisher();
        useCase = new RetireBookUseCase(bookRepository, eventPublisher);
    }

    @Test
    void execute_retires_book() {
        Book book = savedBook();

        useCase.execute(book.getId(), "corr");

        assertThat(bookRepository.findById(book.getId()).get().getStatus()).isEqualTo(BookStatus.RETIRED);
    }

    @Test
    void execute_publishes_BookRetiredEvent() {
        Book book = savedBook();

        useCase.execute(book.getId(), "corr");

        assertThat(eventPublisher.events).hasSize(1);
        assertThat(eventPublisher.events.get(0)).isInstanceOf(BookRetiredEvent.class);
    }

    @Test
    void execute_throws_when_book_not_found() {
        assertThatThrownBy(() -> useCase.execute(BookId.generate(), "corr"))
                .isInstanceOf(BookNotFoundException.class);
    }

    @Test
    void execute_throws_when_book_already_retired() {
        Book book = savedBook();
        useCase.execute(book.getId(), "corr");
        eventPublisher.events.clear();

        assertThatThrownBy(() -> useCase.execute(book.getId(), "corr"))
                .isInstanceOf(BookAlreadyRetiredException.class);
    }

    private Book savedBook() {
        Book book = Book.register(
                new Title("Clean Code"), new Author("Robert", "Martin"),
                Category.TECHNOLOGY, new ISBN("9780132350884"), 2, "corr"
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

    static class CapturingEventPublisher implements DomainEventPublisher {
        final List<DomainEvent> events = new ArrayList<>();

        @Override
        public void publish(List<DomainEvent> incoming) { events.addAll(incoming); }
    }
}
