package com.library.catalog.domain.exception;

import com.library.catalog.domain.model.BookId;

public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(BookId bookId) {
        super("Book not found: " + bookId.value());
    }
}
