package com.library.catalog.interfaces.rest;

import com.library.catalog.domain.exception.BookAlreadyRetiredException;
import com.library.catalog.domain.exception.BookNotFoundException;
import com.library.catalog.domain.exception.InsufficientStockException;
import com.library.catalog.domain.model.BookId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.assertj.core.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleBookNotFound_returns_404() {
        ProblemDetail result = handler.handleBookNotFound(new BookNotFoundException(BookId.generate()));
        assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void handleBookNotFound_includes_message() {
        BookId id = BookId.generate();
        ProblemDetail result = handler.handleBookNotFound(new BookNotFoundException(id));
        assertThat(result.getDetail()).contains(id.value().toString());
    }

    @Test
    void handleBookAlreadyRetired_returns_409() {
        ProblemDetail result = handler.handleBookAlreadyRetired(new BookAlreadyRetiredException(BookId.generate()));
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
    }

    @Test
    void handleInsufficientStock_returns_409() {
        ProblemDetail result = handler.handleInsufficientStock(new InsufficientStockException(BookId.generate()));
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
    }

    @Test
    void handleDataIntegrity_returns_409_with_isbn_message() {
        ProblemDetail result = handler.handleDataIntegrity(new DataIntegrityViolationException("constraint"));
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(result.getDetail()).contains("ISBN");
    }

    @Test
    void handleIllegalArgument_returns_400_with_message() {
        ProblemDetail result = handler.handleIllegalArgument(new IllegalArgumentException("bad input"));
        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(result.getDetail()).isEqualTo("bad input");
    }

    @Test
    void handleGeneric_returns_500() {
        ProblemDetail result = handler.handleGeneric(new RuntimeException("unexpected"));
        assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}
