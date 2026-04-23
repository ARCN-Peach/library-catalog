package com.library.catalog.infrastructure.persistence.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface BookJpaRepository extends JpaRepository<BookJpaEntity, UUID> {

    @Query("""
            SELECT b FROM BookJpaEntity b
            WHERE (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%')))
            AND (:author IS NULL OR LOWER(CONCAT(b.authorFirstName, ' ', b.authorLastName)) LIKE LOWER(CONCAT('%', :author, '%')))
            AND (:category IS NULL OR b.category = :category)
            AND b.status = 'PUBLISHED'
            """)
    Page<BookJpaEntity> search(
            @Param("title") String title,
            @Param("author") String author,
            @Param("category") String category,
            Pageable pageable
    );
}
