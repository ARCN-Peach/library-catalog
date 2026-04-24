package com.library.catalog.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class BookIdTest {

    @Test
    void generate_returns_non_null_id() {
        BookId id = BookId.generate();
        assertThat(id).isNotNull();
        assertThat(id.value()).isNotNull();
    }

    @Test
    void generate_returns_unique_ids() {
        assertThat(BookId.generate()).isNotEqualTo(BookId.generate());
    }

    @Test
    void of_uuid_creates_bookId() {
        UUID uuid = UUID.randomUUID();
        assertThat(BookId.of(uuid).value()).isEqualTo(uuid);
    }

    @Test
    void of_string_creates_bookId() {
        String uuidStr = "550e8400-e29b-41d4-a716-446655440000";
        assertThat(BookId.of(uuidStr).value()).isEqualTo(UUID.fromString(uuidStr));
    }

    @Test
    void of_invalid_string_throws() {
        assertThatThrownBy(() -> BookId.of("not-a-uuid"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_rejects_null() {
        assertThatThrownBy(() -> new BookId(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equality_is_based_on_value() {
        UUID uuid = UUID.randomUUID();
        assertThat(BookId.of(uuid)).isEqualTo(BookId.of(uuid));
    }
}
