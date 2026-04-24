package com.library.catalog.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class AuthorTest {

    @Test
    void fullName_concatenates_first_and_last() {
        Author author = new Author("Robert", "Martin");
        assertThat(author.fullName()).isEqualTo("Robert Martin");
    }

    @Test
    void constructor_trims_whitespace() {
        Author author = new Author("  Robert  ", "  Martin  ");
        assertThat(author.firstName()).isEqualTo("Robert");
        assertThat(author.lastName()).isEqualTo("Martin");
    }

    @Test
    void constructor_rejects_blank_firstName() {
        assertThatThrownBy(() -> new Author("", "Martin"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("first name");
    }

    @Test
    void constructor_rejects_blank_lastName() {
        assertThatThrownBy(() -> new Author("Robert", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("last name");
    }

    @Test
    void constructor_rejects_null_firstName() {
        assertThatThrownBy(() -> new Author(null, "Martin"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_rejects_null_lastName() {
        assertThatThrownBy(() -> new Author("Robert", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equality_is_based_on_value() {
        assertThat(new Author("Robert", "Martin")).isEqualTo(new Author("Robert", "Martin"));
    }
}
