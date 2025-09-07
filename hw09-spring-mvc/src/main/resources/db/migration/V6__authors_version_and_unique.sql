ALTER TABLE authors ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE authors ADD CONSTRAINT uq_authors_full_name UNIQUE (full_name);
