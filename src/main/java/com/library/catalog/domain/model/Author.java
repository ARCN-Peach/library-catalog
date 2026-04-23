package com.library.catalog.domain.model;

public record Author(String firstName, String lastName) {

    public Author {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("Author first name cannot be blank");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Author last name cannot be blank");
        }
        firstName = firstName.trim();
        lastName = lastName.trim();
    }

    public String fullName() {
        return firstName + " " + lastName;
    }
}
