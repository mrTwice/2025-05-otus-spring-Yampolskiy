insert into genres (name) values
                              ('Software Engineering'),
                              ('Refactoring'),
                              ('Clean Code'),
                              ('Java'),
                              ('Architecture')
on conflict (name) do nothing;

insert into authors (full_name) values
                                    ('Martin Fowler'),
                                    ('Robert C. Martin'),
                                    ('Joshua Bloch'),
                                    ('Brian Goetz')
on conflict (full_name) do nothing;

insert into books (title, author_id)
select 'Refactoring', a.id from authors a where a.full_name = 'Martin Fowler'
on conflict do nothing;

insert into books (title, author_id)
select 'Clean Code', a.id from authors a where a.full_name = 'Robert C. Martin'
on conflict do nothing;

insert into books (title, author_id)
select 'Effective Java', a.id from authors a where a.full_name = 'Joshua Bloch'
on conflict do nothing;

insert into books (title, author_id)
select 'Java Concurrency in Practice', a.id from authors a where a.full_name = 'Brian Goetz'
on conflict do nothing;

insert into books_genres (book_id, genre_id)
select b.id, g.id from books b, genres g
where b.title = 'Refactoring' and g.name in ('Software Engineering', 'Refactoring')
on conflict do nothing;

insert into books_genres (book_id, genre_id)
select b.id, g.id from books b, genres g
where b.title = 'Clean Code' and g.name in ('Software Engineering', 'Clean Code')
on conflict do nothing;

insert into books_genres (book_id, genre_id)
select b.id, g.id from books b, genres g
where b.title = 'Effective Java' and g.name in ('Java', 'Architecture')
on conflict do nothing;

insert into books_genres (book_id, genre_id)
select b.id, g.id from books b, genres g
where b.title = 'Java Concurrency in Practice' and g.name in ('Java')
on conflict do nothing;

insert into comments (text, created_at, book_id)
select 'Classic on refactoring', '2024-12-30T10:00:00Z', b.id
from books b where b.title = 'Refactoring';

insert into comments (text, created_at, book_id)
select 'Must read for developers', '2025-01-05T12:00:00Z', b.id
from books b where b.title = 'Clean Code';

insert into comments (text, created_at, book_id)
select 'Great patterns and tips', '2025-02-15T09:30:00Z', b.id
from books b where b.title = 'Effective Java';

insert into comments (text, created_at, book_id)
select 'Concurrency explained well', '2025-05-20T18:45:00Z', b.id
from books b where b.title = 'Java Concurrency in Practice';

insert into comments (text, created_at, book_id)
select 'Refactoring still relevant', '2025-08-01T08:00:00Z', b.id
from books b where b.title = 'Refactoring';
