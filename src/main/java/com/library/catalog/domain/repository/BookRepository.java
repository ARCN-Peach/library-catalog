package com.library.catalog.domain.repository;

import com.library.catalog.domain.model.Book;
import com.library.catalog.domain.model.BookId;
import com.library.catalog.domain.model.SearchCriteria;
import com.library.catalog.domain.model.SearchResult;

import java.util.Optional;

public interface BookRepository {

    void save(Book book);

    Optional<Book> findById(BookId id);

    SearchResult search(SearchCriteria criteria, int page, int pageSize);
}
