package com.library.catalog.domain.model;

import com.library.catalog.domain.event.BookRegisteredEvent;
import com.library.catalog.domain.event.BookRetiredEvent;
import com.library.catalog.domain.event.BookUpdatedEvent;
import com.library.catalog.domain.event.DomainEvent;
import com.library.catalog.domain.exception.BookAlreadyRetiredException;
import com.library.catalog.domain.exception.InsufficientStockException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Book {

    private final BookId id;
    private Title title;
    private Author author;
    private Category category;
    private ISBN isbn;
    private BookStatus status;
    private int totalCopies;
    private int availableStock;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private Book(BookId id, Title title, Author author, Category category,
                 ISBN isbn, BookStatus status, int totalCopies, int availableStock) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.isbn = isbn;
        this.status = status;
        this.totalCopies = totalCopies;
        this.availableStock = availableStock;
    }

    public static Book register(Title title, Author author, Category category,
                                ISBN isbn, int totalCopies, String correlationId) {
        if (totalCopies < 1) {
            throw new IllegalArgumentException("Total copies must be at least 1");
        }
        BookId id = BookId.generate();
        Book book = new Book(id, title, author, category, isbn, BookStatus.PUBLISHED, totalCopies, totalCopies);
        book.domainEvents.add(new BookRegisteredEvent(
                UUID.randomUUID(), id.value(),
                title.value(), author.firstName(), author.lastName(),
                category.name(), isbn.value(), totalCopies,
                Instant.now(), correlationId
        ));
        return book;
    }

    public static Book reconstitute(BookId id, Title title, Author author, Category category,
                                    ISBN isbn, BookStatus status, int totalCopies, int availableStock) {
        return new Book(id, title, author, category, isbn, status, totalCopies, availableStock);
    }

    public void update(Title title, Author author, Category category, ISBN isbn, String correlationId) {
        if (this.status == BookStatus.RETIRED) {
            throw new BookAlreadyRetiredException(this.id);
        }
        this.title = title;
        this.author = author;
        this.category = category;
        this.isbn = isbn;
        domainEvents.add(new BookUpdatedEvent(
                UUID.randomUUID(), id.value(),
                title.value(), author.firstName(), author.lastName(),
                category.name(), isbn.value(),
                Instant.now(), correlationId
        ));
    }

    public void retire(String correlationId) {
        if (this.status == BookStatus.RETIRED) {
            throw new BookAlreadyRetiredException(this.id);
        }
        this.status = BookStatus.RETIRED;
        domainEvents.add(new BookRetiredEvent(
                UUID.randomUUID(), id.value(), Instant.now(), correlationId
        ));
    }

    public void decrementStock() {
        if (availableStock <= 0) {
            throw new InsufficientStockException(this.id);
        }
        availableStock--;
    }

    public void incrementStock() {
        if (availableStock < totalCopies) {
            availableStock++;
        }
    }

    public boolean isAvailable() {
        return status == BookStatus.PUBLISHED && availableStock > 0;
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return Collections.unmodifiableList(events);
    }

    public BookId getId() { return id; }
    public Title getTitle() { return title; }
    public Author getAuthor() { return author; }
    public Category getCategory() { return category; }
    public ISBN getIsbn() { return isbn; }
    public BookStatus getStatus() { return status; }
    public int getTotalCopies() { return totalCopies; }
    public int getAvailableStock() { return availableStock; }
}
