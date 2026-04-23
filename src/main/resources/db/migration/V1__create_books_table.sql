-- V1: Initial catalog schema

CREATE TABLE books (
    id               UUID        PRIMARY KEY,
    title            VARCHAR(255) NOT NULL,
    author_first_name VARCHAR(100) NOT NULL,
    author_last_name  VARCHAR(100) NOT NULL,
    category         VARCHAR(50)  NOT NULL,
    isbn             VARCHAR(13)  NOT NULL UNIQUE,
    status           VARCHAR(20)  NOT NULL DEFAULT 'PUBLISHED',
    total_copies     INT          NOT NULL CHECK (total_copies >= 1),
    available_stock  INT          NOT NULL CHECK (available_stock >= 0)
);

CREATE INDEX idx_books_title    ON books (LOWER(title));
CREATE INDEX idx_books_category ON books (category);
CREATE INDEX idx_books_status   ON books (status);
