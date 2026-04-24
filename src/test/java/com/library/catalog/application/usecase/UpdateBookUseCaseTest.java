package com.library.catalog.application.usecase;

import com.library.catalog.application.dto.UpdateBookCommand;
import com.library.catalog.application.port.DomainEventPublisher;
import com.library.catalog.domain.event.BookUpdatedEvent;
import com.library.catalog.domain.event.DomainEvent;
import com.library.catalog.domain.exception.BookAlreadyRetiredException;
import com.library.catalog.domain.exception.BookNotFoundException;
import com.library.catalog.domain.model.*;
import com.library.catalog.domain.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

class UpdateBookUseCaseTest {

    private InMemoryBookRepository bookRepository;
    private CapturingEventPublisher eventPublisher;
    private UpdateBookUseCase useCase;

    @BeforeEach
    void setUp() {
        bookRepository = new InMemoryBookRepository();
        eventPublisher = new CapturingEventPublisher();
        useCase = new UpdateBookUseCase(bookRepository, eventPublisher);
    }

    @Test
    void execute_updates_book_metadata() {
        Book book = savedBook();

        useCase.execute(new UpdateBookCommand(
                book.getId().value(), "Refactoring", "Martin", "Fowler",
                "TECHNOLOGY", "9780201485677", "corr"
        ));

        Book updated = bookRepository.findById(book.getId()).get();
        assertThat(updated.getTitle().value()).isEqualTo("Refactoring");
        assertThat(updated.getAuthor().firstName()).isEqualTo("Martin");
        assertThat(updated.getAuthor().lastName()).isEqualTo("Fowler");
    }

    @Test
    void execute_publishes_BookUpdatedEvent() {
        Book book = savedBook();

        useCase.execute(new UpdateBookCommand(
                book.getId().value(), "Refactoring", "Martin", "Fowler",
                "TECHNOLOGY", "9780201485677", "corr"
        ));

        assertThat(eventPublisher.events).hasSize(1);
        assertThat(eventPublisher.events.get(0)).isInstanceOf(BookUpdatedEvent.class);
    }

    @Test
    void execute_throws_when_book_not_found() {
        assertThatThrownBy(() -> useCase.execute(new UpdateBookCommand(
                UUID.randomUUID(), "T", "A", "B", "FICTION", "9780132350884", "corr"
        ))).isInstanceOf(BookNotFoundException.class);
    }

    @Test
    void execute_throws_when_book_is_retired() {
        Book book = savedBook();
        book.retire("corr");
        book.pullDomainEvents();
        bookRepository.save(book);

        assertThatThrownBy(() -> useCase.execute(new UpdateBookCommand(
                book.getId().value(), "T", "A", "B", "FICTION", "9780132350884", "corr"
        ))).isInstanceOf(BookAlreadyRetiredException.class);
    }

    @Test
    void execute_throws_when_category_is_invalid() {
        Book book = savedBook();

        assertThatThrownBy(() -> useCase.execute(new UpdateBookCommand(
                book.getId().value(), "T", "A", "B", "INVALID", "9780132350884", "corr"
        ))).isInstanceOf(IllegalArgumentException.class);
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
