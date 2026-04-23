package com.library.catalog.domain.model;

public record SearchCriteria(String title, String author, Category category) {

    public static SearchCriteria empty() {
        return new SearchCriteria(null, null, null);
    }

    public boolean isEmpty() {
        return (title == null || title.isBlank())
                && (author == null || author.isBlank())
                && category == null;
    }
}
