create table comments
(
    id         bigserial primary key,
    text       varchar(2048) not null,
    created_at timestamp     not null default current_timestamp,
    book_id    bigint        not null references books (id) on delete cascade
);

create index idx_comments_book_id on comments (book_id);
