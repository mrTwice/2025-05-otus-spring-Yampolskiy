package ru.otus.hw.migration.cache;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class InMemoryIdMappingService implements IdMappingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryIdMappingService.class);

    private static final String EC_AUTHORS_PG = "mapping.authors.pg";

    private static final String EC_AUTHORS_MONGO = "mapping.authors.mongo";

    private static final String EC_GENRES_PG = "mapping.genres.pg";

    private static final String EC_GENRES_MONGO = "mapping.genres.mongo";

    private static final String EC_BOOKS_PG = "mapping.books.pg";

    private static final String EC_BOOKS_MONGO = "mapping.books.mongo";

    private Map<String, String> authorsPg = new ConcurrentHashMap<>();

    private Map<String, String> authorsMongo = new ConcurrentHashMap<>();

    private Map<String, String> genresPg = new ConcurrentHashMap<>();

    private Map<String, String> genresMongo = new ConcurrentHashMap<>();

    private Map<String, String> booksPg = new ConcurrentHashMap<>();

    private Map<String, String> booksMongo = new ConcurrentHashMap<>();

    @Override
    public Optional<String> findMongoAuthorId(String fullName) {
        return Optional.ofNullable(authorsMongo.get(norm(fullName)));
    }

    @Override
    public void rememberMongoAuthor(String fullName, String mongoId) {
        authorsMongo.put(norm(fullName), mongoId);
    }

    @Override
    public Optional<Long> findPgAuthorId(String fullName) {
        String v = authorsPg.get(norm(fullName));
        return v == null ? Optional.empty() : Optional.of(Long.parseLong(v));
    }

    @Override
    public void rememberPgAuthor(String fullName, Long pgId) {
        authorsPg.put(norm(fullName), String.valueOf(pgId));
    }

    @Override
    public Optional<String> findMongoGenreId(String name) {
        return Optional.ofNullable(genresMongo.get(norm(name)));
    }

    @Override
    public void rememberMongoGenre(String name, String mongoId) {
        genresMongo.put(norm(name), mongoId);
    }

    @Override
    public Optional<Long> findPgGenreId(String name) {
        String v = genresPg.get(norm(name));
        return v == null ? Optional.empty() : Optional.of(Long.parseLong(v));
    }

    @Override
    public void rememberPgGenre(String name, Long pgId) {
        genresPg.put(norm(name), String.valueOf(pgId));
    }

    @Override
    public Optional<String> findMongoBookId(String authorFullName, String title) {
        return Optional.ofNullable(booksMongo.get(bookKey(authorFullName, title)));
    }

    @Override
    public void rememberMongoBook(String authorFullName, String title, String mongoId) {
        booksMongo.put(bookKey(authorFullName, title), mongoId);
    }

    @Override
    public Optional<Long> findPgBookId(String authorFullName, String title) {
        String v = booksPg.get(bookKey(authorFullName, title));
        return v == null ? Optional.empty() : Optional.of(Long.parseLong(v));
    }

    @Override
    public void rememberPgBook(String authorFullName, String title, Long pgId) {
        booksPg.put(bookKey(authorFullName, title), String.valueOf(pgId));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        authorsPg = getOrInit(executionContext, EC_AUTHORS_PG, authorsPg);
        authorsMongo = getOrInit(executionContext, EC_AUTHORS_MONGO, authorsMongo);

        genresPg = getOrInit(executionContext, EC_GENRES_PG, genresPg);
        genresMongo = getOrInit(executionContext, EC_GENRES_MONGO, genresMongo);

        booksPg = getOrInit(executionContext, EC_BOOKS_PG, booksPg);
        booksMongo = getOrInit(executionContext, EC_BOOKS_MONGO, booksMongo);

        log.debug(
                "IdMappingService opened. Sizes: " +
                        "authors(pg={}, " +
                        "mongo={}), " +
                        "genres(pg={}, " +
                        "mongo={}), " +
                        "books(pg={}, " +
                        "mongo={})",
                authorsPg.size(),
                authorsMongo.size(),
                genresPg.size(),
                genresMongo.size(), booksPg.size(), booksMongo.size());
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        executionContext.put(EC_AUTHORS_PG, authorsPg);
        executionContext.put(EC_AUTHORS_MONGO, authorsMongo);
        executionContext.put(EC_GENRES_PG, genresPg);
        executionContext.put(EC_GENRES_MONGO, genresMongo);
        executionContext.put(EC_BOOKS_PG, booksPg);
        executionContext.put(EC_BOOKS_MONGO, booksMongo);
    }

    @Override
    public void close() throws ItemStreamException {
        log.debug("IdMappingService closed.");
    }

    @Override
    public String bookKey(String authorFullName, String title) {
        return norm(authorFullName) + "||" + norm(title);
    }

    private static String norm(String s) {
        if (s == null) {
            return "";
        }
        String n = Normalizer.normalize(s, Normalizer.Form.NFKC);
        n = n.trim().replaceAll("\\s+", " ");
        return n.toLowerCase(Locale.ROOT);
    }

    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> getOrInit(ExecutionContext ctx, String key, Map<String, T> fallback) {
        Object existing = ctx.get(key);
        if (existing instanceof Map<?, ?> map) {
            try {
                return new ConcurrentHashMap<>((Map<String, T>) map);
            } catch (ClassCastException ignored) {
                LOGGER.error("Cannot cast {} to Map", key, ignored);
            }
        }
        return fallback;
    }
}
