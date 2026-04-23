package com.library.catalog.domain.model;

import com.library.catalog.domain.event.BookRegisteredEvent;
import com.library.catalog.domain.event.BookRetiredEvent;
import com.library.catalog.domain.event.BookUpdatedEvent;
import com.library.catalog.domain.event.DomainEvent;
import com.library.catalog.domain.exception.BookAlreadyRetiredException;
import com.library.catalog.domain.exception.InsufficientStockException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class BookTest {

    private static final String CORRELATION = "test-correlation";

    private Book aBook() {
        return Book.register(
                new Title("Clean Code"),
                new Author("Robert", "Martin"),
                Category.TECHNOLOGY,
                new ISBN("9780132350884"),
                3,
                CORRELATION
        );
    }

    @Test
    void register_emits_BookRegisteredEvent() {
        Book book = aBook();
        List<DomainEvent> events = book.pullDomainEvents();

        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(BookRegisteredEvent.class);
        BookRegisteredEvent event = (BookRegisteredEvent) events.get(0);
        assertThat(event.title()).isEqualTo("Clean Code");
        assertThat(event.totalCopies()).isEqualTo(3);
        assertThat(event.correlationId()).isEqualTo(CORRELATION);
    }

    @Test
    void pullDomainEvents_clears_events_after_pull() {
        Book book = aBook();
        book.pullDomainEvents();
        assertThat(book.pullDomainEvents()).isEmpty();
    }

    @Test
    void update_changes_metadata_and_emits_BookUpdatedEvent() {
        Book book = aBook();
        book.pullDomainEvents();

        book.update(new Title("Refactoring"), new Author("Martin", "Fowler"),
                Category.TECHNOLOGY, new ISBN("9780201485677"), CORRELATION);

        assertThat(book.getTitle().value()).isEqualTo("Refactoring");
        assertThat(book.getAuthor().firstName()).isEqualTo("Martin");

        List<DomainEvent> events = book.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(BookUpdatedEvent.class);
    }

    @Test
    void retire_changes_status_and_emits_BookRetiredEvent() {
        Book book = aBook();
        book.pullDomainEvents();

        book.retire(CORRELATION);

        assertThat(book.getStatus()).isEqualTo(BookStatus.RETIRED);
        assertThat(book.isAvailable()).isFalse();

        List<DomainEvent> events = book.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(BookRetiredEvent.class);
    }

    @Test
    void retire_twice_throws_BookAlreadyRetiredException() {
        Book book = aBook();
        book.retire(CORRELATION);

        assertThatThrownBy(() -> book.retire(CORRELATION))
                .isInstanceOf(BookAlreadyRetiredException.class);
    }

    @Test
    void update_retired_book_throws_BookAlreadyRetiredException() {
        Book book = aBook();
        book.retire(CORRELATION);

        assertThatThrownBy(() ->
                book.update(new Title("Other"), new Author("A", "B"),
                        Category.FICTION, new ISBN("9780132350884"), CORRELATION))
                .isInstanceOf(BookAlreadyRetiredException.class);
    }

    @Test
    void decrementStock_reduces_available_stock() {
        Book book = aBook();
        book.decrementStock();
        assertThat(book.getAvailableStock()).isEqualTo(2);
    }

    @Test
    void decrementStock_to_zero_makes_book_unavailable() {
        Book book = aBook();
        book.decrementStock();
        book.decrementStock();
        book.decrementStock();
        assertThat(book.isAvailable()).isFalse();
    }

    @Test
    void decrementStock_below_zero_throws_InsufficientStockException() {
        Book book = aBook();
        book.decrementStock();
        book.decrementStock();
        book.decrementStock();

        assertThatThrownBy(book::decrementStock)
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void incrementStock_restores_availability() {
        Book book = aBook();
        book.decrementStock();
        book.incrementStock();
        assertThat(book.getAvailableStock()).isEqualTo(3);
    }

    @Test
    void incrementStock_does_not_exceed_totalCopies() {
        Book book = aBook();
        book.incrementStock();
        assertThat(book.getAvailableStock()).isEqualTo(3);
    }

    @Test
    void title_validation_rejects_blank() {
        assertThatThrownBy(() -> new Title(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void isbn_validation_rejects_wrong_format() {
        assertThatThrownBy(() -> new ISBN("123"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void isbn_normalizes_hyphens() {
        ISBN isbn = new ISBN("978-0-13-235088-4");
        assertThat(isbn.value()).isEqualTo("9780132350884");
    }

    @Test
    void register_with_zero_copies_throws() {
        assertThatThrownBy(() ->
                Book.register(new Title("T"), new Author("A", "B"),
                        Category.FICTION, new ISBN("9780132350884"), 0, CORRELATION))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
