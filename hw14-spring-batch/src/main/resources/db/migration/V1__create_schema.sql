create table if not exists authors
(
    id        bigserial primary key,
    full_name varchar(255) not null,
    version   bigint       not null default 0
);
alter table authors
    add constraint uq_authors_full_name unique (full_name);

create table if not exists genres
(
    id      bigserial primary key,
    name    varchar(255) not null,
    version bigint       not null default 0
);
alter table genres
    add constraint uq_genres_name unique (name);

create table if not exists books
(
    id        bigserial primary key,
    title     varchar(255) not null,
    author_id bigint       not null references authors (id) on delete restrict,
    version   bigint       not null default 0,
    constraint uq_books_author_title unique (author_id, title)
);
create index if not exists idx_books_author on books (author_id);

create table if not exists books_genres
(
    book_id  bigint not null references books (id) on delete cascade,
    genre_id bigint not null references genres (id) on delete cascade,
    primary key (book_id, genre_id)
);
create index if not exists idx_books_genres_genre on books_genres (genre_id);

create table if not exists comments
(
    id         bigserial primary key,
    text       varchar(2048)            not null,
    created_at timestamp with time zone not null default now(),
    book_id    bigint                   not null references books (id) on delete cascade,
    version    bigint                   not null default 0
);
create index if not exists idx_comments_book on comments (book_id);
create index if not exists idx_comments_created_at on comments (created_at);
