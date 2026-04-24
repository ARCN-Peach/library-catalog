package com.library.catalog.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class SearchCriteriaTest {

    @Test
    void empty_creates_criteria_with_all_nulls() {
        SearchCriteria criteria = SearchCriteria.empty();
        assertThat(criteria.title()).isNull();
        assertThat(criteria.author()).isNull();
        assertThat(criteria.category()).isNull();
    }

    @Test
    void isEmpty_returns_true_when_all_null() {
        assertThat(SearchCriteria.empty().isEmpty()).isTrue();
    }

    @Test
    void isEmpty_returns_false_when_title_set() {
        assertThat(new SearchCriteria("clean code", null, null).isEmpty()).isFalse();
    }

    @Test
    void isEmpty_returns_false_when_author_set() {
        assertThat(new SearchCriteria(null, "martin", null).isEmpty()).isFalse();
    }

    @Test
    void isEmpty_returns_false_when_category_set() {
        assertThat(new SearchCriteria(null, null, Category.TECHNOLOGY).isEmpty()).isFalse();
    }

    @Test
    void isEmpty_returns_true_when_strings_are_blank() {
        assertThat(new SearchCriteria("   ", "   ", null).isEmpty()).isTrue();
    }
}
