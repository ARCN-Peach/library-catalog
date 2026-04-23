package com.library.catalog.application.usecase;

import com.library.catalog.application.dto.UpdateBookCommand;
import com.library.catalog.application.port.DomainEventPublisher;
import com.library.catalog.domain.exception.BookNotFoundException;
import com.library.catalog.domain.model.*;
import com.library.catalog.domain.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateBookUseCase {

    private final BookRepository bookRepository;
    private final DomainEventPublisher eventPublisher;

    public UpdateBookUseCase(BookRepository bookRepository, DomainEventPublisher eventPublisher) {
        this.bookRepository = bookRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void execute(UpdateBookCommand command) {
        BookId bookId = BookId.of(command.bookId());
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));

        Title title = new Title(command.title());
        Author author = new Author(command.authorFirstName(), command.authorLastName());
        Category category = Category.valueOf(command.category().toUpperCase());
        ISBN isbn = new ISBN(command.isbn());

        book.update(title, author, category, isbn, command.correlationId());
        bookRepository.save(book);
        eventPublisher.publish(book.pullDomainEvents());
    }
}
