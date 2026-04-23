package com.library.catalog.infrastructure.persistence.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface BookJpaRepository extends JpaRepository<BookJpaEntity, UUID> {

    @Query(value = """
            SELECT * FROM books
            WHERE (CAST(:title AS TEXT) IS NULL OR LOWER(title) LIKE LOWER('%' || CAST(:title AS TEXT) || '%'))
            AND   (CAST(:author AS TEXT) IS NULL OR LOWER(author_first_name || ' ' || author_last_name) LIKE LOWER('%' || CAST(:author AS TEXT) || '%'))
            AND   (CAST(:category AS TEXT) IS NULL OR category = CAST(:category AS TEXT))
            AND   status = 'PUBLISHED'
            """,
            countQuery = """
            SELECT COUNT(*) FROM books
            WHERE (CAST(:title AS TEXT) IS NULL OR LOWER(title) LIKE LOWER('%' || CAST(:title AS TEXT) || '%'))
            AND   (CAST(:author AS TEXT) IS NULL OR LOWER(author_first_name || ' ' || author_last_name) LIKE LOWER('%' || CAST(:author AS TEXT) || '%'))
            AND   (CAST(:category AS TEXT) IS NULL OR category = CAST(:category AS TEXT))
            AND   status = 'PUBLISHED'
            """,
            nativeQuery = true)
    Page<BookJpaEntity> search(
            @Param("title") String title,
            @Param("author") String author,
            @Param("category") String category,
            Pageable pageable
    );
}
