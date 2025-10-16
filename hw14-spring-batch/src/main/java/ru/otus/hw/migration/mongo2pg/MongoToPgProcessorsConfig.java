package ru.otus.hw.migration.mongo2pg;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ru.otus.hw.domain.mongo.MongoAuthor;
import ru.otus.hw.domain.mongo.MongoBook;
import ru.otus.hw.domain.mongo.MongoComment;
import ru.otus.hw.domain.mongo.MongoGenre;

import ru.otus.hw.domain.pg.JpaAuthor;
import ru.otus.hw.domain.pg.JpaBook;
import ru.otus.hw.domain.pg.JpaComment;
import ru.otus.hw.domain.pg.JpaGenre;

import ru.otus.hw.migration.cache.IdMappingService;
import ru.otus.hw.migration.pg2mongo.MissingMappingException;
import ru.otus.hw.repo.mongo.MongoAuthorRepository;
import ru.otus.hw.repo.mongo.MongoBookRepository;
import ru.otus.hw.repo.mongo.MongoGenreRepository;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class MongoToPgProcessorsConfig {

    private final IdMappingService idMapping;

    private final MongoAuthorRepository mongoAuthorRepo;

    private final MongoGenreRepository  mongoGenreRepo;

    private final MongoBookRepository   mongoBookRepo;


    @Bean
    public ItemProcessor<MongoAuthor, JpaAuthor> authorMongoToPgProcessor() {
        return this::mapAuthor;
    }

    @Bean
    public ItemProcessor<MongoGenre, JpaGenre> genreMongoToPgProcessor() {
        return this::mapGenre;
    }

    @Bean
    public ItemProcessor<MongoBook, JpaBook> bookMongoToPgProcessor() {
        return this::mapBook;
    }

    @Bean
    public ItemProcessor<MongoComment, JpaComment> commentMongoToPgProcessor() {
        return this::mapComment;
    }


    private JpaAuthor mapAuthor(MongoAuthor src) {
        JpaAuthor tgt = new JpaAuthor();
        idMapping.findPgAuthorId(src.getFullName()).ifPresent(tgt::setId);
        tgt.setFullName(src.getFullName());
        tgt.setVersion(src.getVersion());
        return tgt;
    }

    private JpaGenre mapGenre(MongoGenre src) {
        JpaGenre tgt = new JpaGenre();
        idMapping.findPgGenreId(src.getName()).ifPresent(tgt::setId);
        tgt.setName(src.getName());
        tgt.setVersion(src.getVersion());
        return tgt;
    }

    private JpaBook mapBook(MongoBook src) {
        String authorFullName = resolveMongoAuthorFullName(src.getAuthorId());
        Long pgAuthorId = resolvePgAuthorId(authorFullName);

        JpaAuthor jpaAuthor = buildJpaAuthorStub(pgAuthorId, authorFullName);
        Set<JpaGenre> jpaGenres = mapGenreIdsToJpa(src.getGenresIds());

        JpaBook tgt = new JpaBook();
        tgt.setTitle(src.getTitle());
        tgt.setJpaAuthor(jpaAuthor);
        tgt.replaceGenres(jpaGenres);
        tgt.setVersion(src.getVersion());
        return tgt;
    }

    private JpaComment mapComment(MongoComment src) {
        MongoBook mBook = loadMongoBook(src.getBookId());
        String authorFullName = resolveMongoAuthorFullName(mBook.getAuthorId());
        Long pgBookId = resolvePgBookId(authorFullName, mBook.getTitle());

        JpaBook jpaBook = buildJpaBookStub(pgBookId);

        JpaComment tgt = new JpaComment();
        tgt.setJpaBook(jpaBook);
        tgt.setText(src.getText());
        tgt.setCreatedAt(src.getCreatedAt());
        tgt.setVersion(src.getVersion());
        return tgt;
    }

    private MongoBook loadMongoBook(String bookId) {
        return mongoBookRepo.findById(bookId)
                .orElseThrow(() -> new MissingMappingException(
                        "Mongo book not found by id=" + bookId));
    }

    private String resolveMongoAuthorFullName(String authorId) {
        return mongoAuthorRepo.findById(authorId)
                .map(MongoAuthor::getFullName)
                .orElseThrow(() -> new MissingMappingException(
                        "Mongo author not found by id=" + authorId));
    }

    private Long resolvePgAuthorId(String authorFullName) {
        return idMapping.findPgAuthorId(authorFullName)
                .orElseThrow(() -> new MissingMappingException(
                        "PG AuthorId not found for fullName=" + authorFullName +
                                ". Ensure authorsMongoToPgStep ran and stored mapping."));
    }

    private Long resolvePgBookId(String authorFullName, String title) {
        return idMapping.findPgBookId(authorFullName, title)
                .orElseThrow(() -> new MissingMappingException(
                        "PG BookId not found for (" + authorFullName + ", " + title + "). " +
                                "Ensure booksMongoToPgStep ran and stored mapping."));
    }

    private Long resolvePgGenreIdByName(String name) {
        return idMapping.findPgGenreId(name)
                .orElseThrow(() -> new MissingMappingException(
                        "PG GenreId not found for name=" + name +
                                ". Ensure genresMongoToPgStep ran and stored mapping."));
    }

    private JpaAuthor buildJpaAuthorStub(Long id, String fullName) {
        JpaAuthor a = new JpaAuthor();
        a.setId(id);
        a.setFullName(fullName);
        return a;
    }

    private JpaBook buildJpaBookStub(Long id) {
        JpaBook b = new JpaBook();
        b.setId(id);
        return b;
    }

    private Set<JpaGenre> mapGenreIdsToJpa(Collection<String> genreIds) {
        Set<JpaGenre> result = new LinkedHashSet<>();
        for (String gid : genreIds) {
            String name = mongoGenreRepo.findById(gid)
                    .map(MongoGenre::getName)
                    .orElseThrow(() -> new MissingMappingException(
                            "Mongo genre not found by id=" + gid));
            Long pgGenreId = resolvePgGenreIdByName(name);

            JpaGenre g = new JpaGenre();
            g.setId(pgGenreId);
            g.setName(name);
            result.add(g);
        }
        return result;
    }
}
