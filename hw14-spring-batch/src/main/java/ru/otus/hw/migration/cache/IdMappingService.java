package ru.otus.hw.migration.cache;


import org.springframework.batch.item.ItemStream;

import java.util.Optional;

public interface IdMappingService extends ItemStream {

    Optional<String> findMongoAuthorId(String fullName);

    void rememberMongoAuthor(String fullName, String mongoId);

    Optional<Long> findPgAuthorId(String fullName);

    void rememberPgAuthor(String fullName, Long pgId);

    Optional<String> findMongoGenreId(String name);

    void rememberMongoGenre(String name, String mongoId);

    Optional<Long> findPgGenreId(String name);

    void rememberPgGenre(String name, Long pgId);

    Optional<String> findMongoBookId(String authorFullName, String title);

    void rememberMongoBook(String authorFullName, String title, String mongoId);

    Optional<Long> findPgBookId(String authorFullName, String title);

    void rememberPgBook(String authorFullName, String title, Long pgId);

    String bookKey(String authorFullName, String title);
}
