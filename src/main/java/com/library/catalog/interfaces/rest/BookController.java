package com.library.catalog.interfaces.rest;

import com.library.catalog.application.dto.*;
import com.library.catalog.application.usecase.*;
import com.library.catalog.domain.model.BookId;
import com.library.catalog.interfaces.rest.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/books")
@Tag(name = "Catalog", description = "Book catalog management")
public class BookController {

    private final RegisterBookUseCase registerBookUseCase;
    private final UpdateBookUseCase updateBookUseCase;
    private final RetireBookUseCase retireBookUseCase;
    private final SearchBooksUseCase searchBooksUseCase;
    private final GetBookUseCase getBookUseCase;

    public BookController(RegisterBookUseCase registerBookUseCase,
                          UpdateBookUseCase updateBookUseCase,
                          RetireBookUseCase retireBookUseCase,
                          SearchBooksUseCase searchBooksUseCase,
                          GetBookUseCase getBookUseCase) {
        this.registerBookUseCase = registerBookUseCase;
        this.updateBookUseCase = updateBookUseCase;
        this.retireBookUseCase = retireBookUseCase;
        this.searchBooksUseCase = searchBooksUseCase;
        this.getBookUseCase = getBookUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new book in the catalog")
    public BookResponse register(@Valid @RequestBody RegisterBookRequest request,
                                 @RequestHeader(value = "X-Correlation-Id", defaultValue = "") String correlationId) {
        RegisterBookCommand command = new RegisterBookCommand(
                request.title(), request.authorFirstName(), request.authorLastName(),
                request.category(), request.isbn(), request.totalCopies(),
                resolveCorrelationId(correlationId)
        );
        RegisterBookResult result = registerBookUseCase.execute(command);
        return new BookResponse(
                result.bookId(), request.title(), request.authorFirstName(), request.authorLastName(),
                request.category(), request.isbn(), "PUBLISHED", request.totalCopies(), request.totalCopies()
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a book by ID")
    public BookResponse getById(@PathVariable UUID id) {
        BookSummary summary = getBookUseCase.execute(id);
        return toResponse(summary);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Update a book's metadata")
    public void update(@PathVariable UUID id,
                       @Valid @RequestBody UpdateBookRequest request,
                       @RequestHeader(value = "X-Correlation-Id", defaultValue = "") String correlationId) {
        UpdateBookCommand command = new UpdateBookCommand(
                id, request.title(), request.authorFirstName(), request.authorLastName(),
                request.category(), request.isbn(), resolveCorrelationId(correlationId)
        );
        updateBookUseCase.execute(command);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Retire a book from the catalog")
    public void retire(@PathVariable UUID id,
                       @RequestHeader(value = "X-Correlation-Id", defaultValue = "") String correlationId) {
        retireBookUseCase.execute(BookId.of(id), resolveCorrelationId(correlationId));
    }

    @GetMapping("/search")
    @Operation(summary = "Search books by title, author or category")
    public SearchBooksResponse search(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        SearchBooksQuery query = new SearchBooksQuery(title, author, category, page, pageSize);
        SearchBooksResult result = searchBooksUseCase.execute(query);

        List<BookResponse> responses = result.books().stream()
                .map(this::toResponse)
                .toList();

        return new SearchBooksResponse(responses, result.totalFound(), result.page(), result.pageSize());
    }

    private BookResponse toResponse(BookSummary s) {
        return new BookResponse(s.id(), s.title(), s.authorFirstName(), s.authorLastName(),
                s.category(), s.isbn(), s.status(), s.totalCopies(), s.availableStock());
    }

    private String resolveCorrelationId(String header) {
        return (header == null || header.isBlank()) ? UUID.randomUUID().toString() : header;
    }
}
