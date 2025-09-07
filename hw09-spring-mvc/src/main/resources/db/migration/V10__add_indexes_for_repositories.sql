CREATE INDEX IF NOT EXISTS idx_books_author_id ON books(author_id);
CREATE INDEX IF NOT EXISTS idx_books_genres_genre_id ON books_genres(genre_id);
CREATE INDEX IF NOT EXISTS idx_comments_book_created_at ON comments(book_id, created_at DESC);
