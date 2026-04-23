package com.library.catalog.application.usecase;

import com.library.catalog.domain.exception.BookNotFoundException;
import com.library.catalog.domain.exception.InsufficientStockException;
import com.library.catalog.domain.model.Book;
import com.library.catalog.domain.model.BookId;
import com.library.catalog.domain.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateBookStockUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateBookStockUseCase.class);

    private final BookRepository bookRepository;

    public UpdateBookStockUseCase(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional
    public void decrementStock(UUID bookId, String correlationId) {
        BookId id = BookId.of(bookId);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
        try {
            book.decrementStock();
            bookRepository.save(book);
        } catch (InsufficientStockException e) {
            log.warn("Stock inconsistency detected for book {} [correlationId={}]: {}",
                    bookId, correlationId, e.getMessage());
        }
    }

    @Transactional
    public void incrementStock(UUID bookId, String correlationId) {
        BookId id = BookId.of(bookId);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
        book.incrementStock();
        bookRepository.save(book);
    }
}
