package com.library.catalog.domain.exception;

import com.library.catalog.domain.model.BookId;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(BookId bookId) {
        super("No available stock for book: " + bookId.value());
    }
}
