package com.library.catalog.domain.exception;

import com.library.catalog.domain.model.BookId;

public class BookAlreadyRetiredException extends RuntimeException {

    public BookAlreadyRetiredException(BookId bookId) {
        super("Book is already retired: " + bookId.value());
    }
}
