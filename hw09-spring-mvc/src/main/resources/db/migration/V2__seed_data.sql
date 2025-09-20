
-- Authors
INSERT INTO authors(full_name)
VALUES ('Author_1'),
       ('Author_2'),
       ('Author_3');

-- Genres
INSERT INTO genres(name)
VALUES ('Genre_1'),
       ('Genre_2'),
       ('Genre_3'),
       ('Genre_4'),
       ('Genre_5'),
       ('Genre_6');

-- Books (bind author_id by name)
INSERT INTO books(title, author_id)
SELECT 'BookTitle_1', a.id
FROM authors a
WHERE a.full_name = 'Author_1';

INSERT INTO books(title, author_id)
SELECT 'BookTitle_2', a.id
FROM authors a
WHERE a.full_name = 'Author_2';

INSERT INTO books(title, author_id)
SELECT 'BookTitle_3', a.id
FROM authors a
WHERE a.full_name = 'Author_3';

-- Books ↔ Genres links
INSERT INTO books_genres(book_id, genre_id)
SELECT b.id, g.id
FROM books b
         JOIN authors a ON a.id = b.author_id
         JOIN genres g ON g.name = 'Genre_1'
WHERE b.title = 'BookTitle_1'
  AND a.full_name = 'Author_1';

INSERT INTO books_genres(book_id, genre_id)
SELECT b.id, g.id
FROM books b
         JOIN authors a ON a.id = b.author_id
         JOIN genres g ON g.name = 'Genre_2'
WHERE b.title = 'BookTitle_1'
  AND a.full_name = 'Author_1';

INSERT INTO books_genres(book_id, genre_id)
SELECT b.id, g.id
FROM books b
         JOIN authors a ON a.id = b.author_id
         JOIN genres g ON g.name = 'Genre_3'
WHERE b.title = 'BookTitle_2'
  AND a.full_name = 'Author_2';

INSERT INTO books_genres(book_id, genre_id)
SELECT b.id, g.id
FROM books b
         JOIN authors a ON a.id = b.author_id
         JOIN genres g ON g.name = 'Genre_4'
WHERE b.title = 'BookTitle_2'
  AND a.full_name = 'Author_2';

INSERT INTO books_genres(book_id, genre_id)
SELECT b.id, g.id
FROM books b
         JOIN authors a ON a.id = b.author_id
         JOIN genres g ON g.name = 'Genre_5'
WHERE b.title = 'BookTitle_3'
  AND a.full_name = 'Author_3';

INSERT INTO books_genres(book_id, genre_id)
SELECT b.id, g.id
FROM books b
         JOIN authors a ON a.id = b.author_id
         JOIN genres g ON g.name = 'Genre_6'
WHERE b.title = 'BookTitle_3'
  AND a.full_name = 'Author_3';

-- Comments (bind book_id by (title, author))
INSERT INTO comments(text, created_at, book_id)
SELECT 'Great book!', NOW(), b.id
FROM books b
         JOIN authors a ON a.id = b.author_id
WHERE b.title = 'BookTitle_1'
  AND a.full_name = 'Author_1';

INSERT INTO comments(text, created_at, book_id)
SELECT 'Not my cup of tea', NOW(), b.id
FROM books b
         JOIN authors a ON a.id = b.author_id
WHERE b.title = 'BookTitle_1'
  AND a.full_name = 'Author_1';

INSERT INTO comments(text, created_at, book_id)
SELECT 'Awesome read', NOW(), b.id
FROM books b
         JOIN authors a ON a.id = b.author_id
WHERE b.title = 'BookTitle_2'
  AND a.full_name = 'Author_2';
