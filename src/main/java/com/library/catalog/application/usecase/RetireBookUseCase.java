package com.library.catalog.application.usecase;

import com.library.catalog.application.port.DomainEventPublisher;
import com.library.catalog.domain.exception.BookNotFoundException;
import com.library.catalog.domain.model.Book;
import com.library.catalog.domain.model.BookId;
import com.library.catalog.domain.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RetireBookUseCase {

    private final BookRepository bookRepository;
    private final DomainEventPublisher eventPublisher;

    public RetireBookUseCase(BookRepository bookRepository, DomainEventPublisher eventPublisher) {
        this.bookRepository = bookRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void execute(BookId bookId, String correlationId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));

        book.retire(correlationId);
        bookRepository.save(book);
        eventPublisher.publish(book.pullDomainEvents());
    }
}
