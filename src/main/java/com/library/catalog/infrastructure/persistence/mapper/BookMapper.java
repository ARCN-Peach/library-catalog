package com.library.catalog.infrastructure.persistence.mapper;

import com.library.catalog.domain.model.*;
import com.library.catalog.infrastructure.persistence.jpa.BookJpaEntity;

public final class BookMapper {

    private BookMapper() {
    }

    public static Book toDomain(BookJpaEntity entity) {
        return Book.reconstitute(
                BookId.of(entity.getId()),
                new Title(entity.getTitle()),
                new Author(entity.getAuthorFirstName(), entity.getAuthorLastName()),
                Category.valueOf(entity.getCategory()),
                new ISBN(entity.getIsbn()),
                BookStatus.valueOf(entity.getStatus()),
                entity.getTotalCopies(),
                entity.getAvailableStock()
        );
    }

    public static BookJpaEntity toJpa(Book book) {
        BookJpaEntity entity = new BookJpaEntity();
        entity.setId(book.getId().value());
        entity.setTitle(book.getTitle().value());
        entity.setAuthorFirstName(book.getAuthor().firstName());
        entity.setAuthorLastName(book.getAuthor().lastName());
        entity.setCategory(book.getCategory().name());
        entity.setIsbn(book.getIsbn().value());
        entity.setStatus(book.getStatus().name());
        entity.setTotalCopies(book.getTotalCopies());
        entity.setAvailableStock(book.getAvailableStock());
        return entity;
    }
}
