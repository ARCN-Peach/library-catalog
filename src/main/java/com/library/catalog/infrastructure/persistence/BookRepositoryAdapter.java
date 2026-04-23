package com.library.catalog.infrastructure.persistence;

import com.library.catalog.domain.model.*;
import com.library.catalog.domain.repository.BookRepository;
import com.library.catalog.infrastructure.persistence.jpa.BookJpaEntity;
import com.library.catalog.infrastructure.persistence.jpa.BookJpaRepository;
import com.library.catalog.infrastructure.persistence.mapper.BookMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BookRepositoryAdapter implements BookRepository {

    private final BookJpaRepository jpaRepository;

    public BookRepositoryAdapter(BookJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Book book) {
        jpaRepository.save(BookMapper.toJpa(book));
    }

    @Override
    public Optional<Book> findById(BookId id) {
        return jpaRepository.findById(id.value()).map(BookMapper::toDomain);
    }

    @Override
    public SearchResult search(SearchCriteria criteria, int page, int pageSize) {
        PageRequest pageRequest = PageRequest.of(page, pageSize);
        Page<BookJpaEntity> result = jpaRepository.search(
                blankToNull(criteria.title()),
                blankToNull(criteria.author()),
                criteria.category() != null ? criteria.category().name() : null,
                pageRequest
        );

        List<Book> books = result.getContent().stream()
                .map(BookMapper::toDomain)
                .toList();

        return new SearchResult(books, result.getTotalElements(), page, pageSize);
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
