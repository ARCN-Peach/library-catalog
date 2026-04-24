package com.library.catalog.infrastructure.persistence.mapper;

import com.library.catalog.domain.model.*;
import com.library.catalog.infrastructure.persistence.jpa.BookJpaEntity;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class BookMapperTest {

    @Test
    void toJpa_maps_all_fields() {
        Book book = Book.register(
                new Title("Clean Code"), new Author("Robert", "Martin"),
                Category.TECHNOLOGY, new ISBN("9780132350884"), 3, "corr"
        );
        book.pullDomainEvents();

        BookJpaEntity entity = BookMapper.toJpa(book);

        assertThat(entity.getId()).isEqualTo(book.getId().value());
        assertThat(entity.getTitle()).isEqualTo("Clean Code");
        assertThat(entity.getAuthorFirstName()).isEqualTo("Robert");
        assertThat(entity.getAuthorLastName()).isEqualTo("Martin");
        assertThat(entity.getCategory()).isEqualTo("TECHNOLOGY");
        assertThat(entity.getIsbn()).isEqualTo("9780132350884");
        assertThat(entity.getStatus()).isEqualTo("PUBLISHED");
        assertThat(entity.getTotalCopies()).isEqualTo(3);
        assertThat(entity.getAvailableStock()).isEqualTo(3);
    }

    @Test
    void toDomain_maps_all_fields() {
        UUID id = UUID.randomUUID();
        BookJpaEntity entity = new BookJpaEntity();
        entity.setId(id);
        entity.setTitle("Clean Code");
        entity.setAuthorFirstName("Robert");
        entity.setAuthorLastName("Martin");
        entity.setCategory("TECHNOLOGY");
        entity.setIsbn("9780132350884");
        entity.setStatus("PUBLISHED");
        entity.setTotalCopies(3);
        entity.setAvailableStock(2);

        Book book = BookMapper.toDomain(entity);

        assertThat(book.getId().value()).isEqualTo(id);
        assertThat(book.getTitle().value()).isEqualTo("Clean Code");
        assertThat(book.getAuthor().firstName()).isEqualTo("Robert");
        assertThat(book.getAuthor().lastName()).isEqualTo("Martin");
        assertThat(book.getCategory()).isEqualTo(Category.TECHNOLOGY);
        assertThat(book.getIsbn().value()).isEqualTo("9780132350884");
        assertThat(book.getStatus()).isEqualTo(BookStatus.PUBLISHED);
        assertThat(book.getTotalCopies()).isEqualTo(3);
        assertThat(book.getAvailableStock()).isEqualTo(2);
    }

    @Test
    void roundtrip_book_to_jpa_to_domain_preserves_fields() {
        Book original = Book.register(
                new Title("DDD"), new Author("Eric", "Evans"),
                Category.TECHNOLOGY, new ISBN("9780321125217"), 5, "corr"
        );
        original.pullDomainEvents();

        Book restored = BookMapper.toDomain(BookMapper.toJpa(original));

        assertThat(restored.getId()).isEqualTo(original.getId());
        assertThat(restored.getTitle()).isEqualTo(original.getTitle());
        assertThat(restored.getAuthor()).isEqualTo(original.getAuthor());
        assertThat(restored.getCategory()).isEqualTo(original.getCategory());
        assertThat(restored.getIsbn()).isEqualTo(original.getIsbn());
        assertThat(restored.getStatus()).isEqualTo(original.getStatus());
        assertThat(restored.getTotalCopies()).isEqualTo(original.getTotalCopies());
        assertThat(restored.getAvailableStock()).isEqualTo(original.getAvailableStock());
    }
}
