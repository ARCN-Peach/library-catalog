package com.library.catalog.application.usecase;

import com.library.catalog.application.dto.RegisterBookCommand;
import com.library.catalog.application.dto.RegisterBookResult;
import com.library.catalog.application.port.DomainEventPublisher;
import com.library.catalog.domain.model.*;
import com.library.catalog.domain.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterBookUseCase {

    private final BookRepository bookRepository;
    private final DomainEventPublisher eventPublisher;

    public RegisterBookUseCase(BookRepository bookRepository, DomainEventPublisher eventPublisher) {
        this.bookRepository = bookRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public RegisterBookResult execute(RegisterBookCommand command) {
        Title title = new Title(command.title());
        Author author = new Author(command.authorFirstName(), command.authorLastName());
        Category category = Category.valueOf(command.category().toUpperCase());
        ISBN isbn = new ISBN(command.isbn());

        Book book = Book.register(title, author, category, isbn, command.totalCopies(), command.correlationId());
        bookRepository.save(book);
        eventPublisher.publish(book.pullDomainEvents());

        return new RegisterBookResult(book.getId().value());
    }
}
