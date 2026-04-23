package com.library.catalog.application.usecase;

import com.library.catalog.application.dto.BookSummary;
import com.library.catalog.domain.exception.BookNotFoundException;
import com.library.catalog.domain.model.Book;
import com.library.catalog.domain.model.BookId;
import com.library.catalog.domain.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GetBookUseCase {

    private final BookRepository bookRepository;

    public GetBookUseCase(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public BookSummary execute(UUID bookId) {
        BookId id = BookId.of(bookId);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
        return toSummary(book);
    }

    private BookSummary toSummary(Book book) {
        return new BookSummary(
                book.getId().value(),
                book.getTitle().value(),
                book.getAuthor().firstName(),
                book.getAuthor().lastName(),
                book.getCategory().name(),
                book.getIsbn().value(),
                book.getStatus().name(),
                book.getTotalCopies(),
                book.getAvailableStock()
        );
    }
}
