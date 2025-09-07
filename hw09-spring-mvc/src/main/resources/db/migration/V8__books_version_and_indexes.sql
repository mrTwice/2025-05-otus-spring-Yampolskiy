ALTER TABLE books ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
CREATE INDEX IF NOT EXISTS idx_books_author_id ON books(author_id);
ALTER TABLE books ADD CONSTRAINT uq_books_author_title UNIQUE (author_id, title);
