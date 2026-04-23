package com.library.catalog.application.usecase;

import com.library.catalog.application.dto.RegisterBookCommand;
import com.library.catalog.application.dto.RegisterBookResult;
import com.library.catalog.application.port.DomainEventPublisher;
import com.library.catalog.domain.event.BookRegisteredEvent;
import com.library.catalog.domain.event.DomainEvent;
import com.library.catalog.domain.model.*;
import com.library.catalog.domain.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

class RegisterBookUseCaseTest {

    private BookRepository bookRepository;
    private CapturingEventPublisher eventPublisher;
    private RegisterBookUseCase useCase;

    @BeforeEach
    void setUp() {
        bookRepository = new InMemoryBookRepository();
        eventPublisher = new CapturingEventPublisher();
        useCase = new RegisterBookUseCase(bookRepository, eventPublisher);
    }

    @Test
    void execute_saves_book_and_returns_id() {
        RegisterBookCommand command = new RegisterBookCommand(
                "Clean Code", "Robert", "Martin",
                "TECHNOLOGY", "9780132350884", 2, "corr-1"
        );

        RegisterBookResult result = useCase.execute(command);

        assertThat(result.bookId()).isNotNull();
        Optional<Book> saved = bookRepository.findById(BookId.of(result.bookId()));
        assertThat(saved).isPresent();
        assertThat(saved.get().getTitle().value()).isEqualTo("Clean Code");
        assertThat(saved.get().getTotalCopies()).isEqualTo(2);
        assertThat(saved.get().getAvailableStock()).isEqualTo(2);
        assertThat(saved.get().getStatus()).isEqualTo(BookStatus.PUBLISHED);
    }

    @Test
    void execute_publishes_BookRegisteredEvent() {
        RegisterBookCommand command = new RegisterBookCommand(
                "Domain-Driven Design", "Eric", "Evans",
                "TECHNOLOGY", "9780321125217", 1, "corr-2"
        );

        useCase.execute(command);

        assertThat(eventPublisher.getEvents()).hasSize(1);
        assertThat(eventPublisher.getEvents().get(0)).isInstanceOf(BookRegisteredEvent.class);
        BookRegisteredEvent event = (BookRegisteredEvent) eventPublisher.getEvents().get(0);
        assertThat(event.title()).isEqualTo("Domain-Driven Design");
        assertThat(event.correlationId()).isEqualTo("corr-2");
    }

    @Test
    void execute_rejects_invalid_category() {
        RegisterBookCommand command = new RegisterBookCommand(
                "Book", "A", "B", "INVALID_CATEGORY", "9780132350884", 1, "corr"
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void execute_rejects_invalid_isbn() {
        RegisterBookCommand command = new RegisterBookCommand(
                "Book", "A", "B", "FICTION", "123", 1, "corr"
        );

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ── In-memory doubles ────────────────────────────────────────────────────

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
            List<Book> all = store.values().stream()
                    .filter(b -> b.getStatus() == BookStatus.PUBLISHED)
                    .filter(b -> criteria.title() == null ||
                            b.getTitle().value().toLowerCase().contains(criteria.title()))
                    .filter(b -> criteria.author() == null ||
                            b.getAuthor().fullName().toLowerCase().contains(criteria.author()))
                    .filter(b -> criteria.category() == null ||
                            b.getCategory() == criteria.category())
                    .toList();

            int from = page * pageSize;
            int to = Math.min(from + pageSize, all.size());
            List<Book> pageBooks = from >= all.size() ? List.of() : all.subList(from, to);
            return new SearchResult(pageBooks, all.size(), page, pageSize);
        }
    }

    static class CapturingEventPublisher implements DomainEventPublisher {
        private final List<DomainEvent> events = new ArrayList<>();

        @Override
        public void publish(List<DomainEvent> incoming) {
            events.addAll(incoming);
        }

        List<DomainEvent> getEvents() {
            return Collections.unmodifiableList(events);
        }
    }
}
