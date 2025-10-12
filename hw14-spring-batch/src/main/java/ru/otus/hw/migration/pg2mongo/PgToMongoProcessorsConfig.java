package ru.otus.hw.migration.pg2mongo;


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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class PgToMongoProcessorsConfig {

    private final IdMappingService idMapping;


    @Bean
    public ItemProcessor<JpaAuthor, MongoAuthor> authorPgToMongoProcessor() {
        return this::mapAuthor;
    }

    @Bean
    public ItemProcessor<JpaGenre, MongoGenre> genrePgToMongoProcessor() {
        return this::mapGenre;
    }

    @Bean
    public ItemProcessor<JpaBook, MongoBook> bookPgToMongoProcessor() {
        return this::mapBook;
    }

    @Bean
    public ItemProcessor<JpaComment, MongoComment> commentPgToMongoProcessor() {
        return this::mapComment;
    }

    private MongoAuthor mapAuthor(JpaAuthor pg) {
        MongoAuthor mongo = new MongoAuthor();
        mongo.setId(null);
        mongo.setFullName(pg.getFullName());
        mongo.setVersion(pg.getVersion());
        return mongo;
    }

    private MongoGenre mapGenre(JpaGenre pg) {
        MongoGenre mongo = new MongoGenre();
        mongo.setId(null);
        mongo.setName(pg.getName());
        mongo.setVersion(pg.getVersion());
        return mongo;
    }

    private MongoBook mapBook(JpaBook pg) {
        String authorFullName = pg.getJpaAuthor().getFullName();
        String authorIdMongo = findMongoAuthorIdOrThrow(authorFullName);

        List<String> genresIds = mapGenresToIds(pg.getJpaGenres());

        MongoBook mongo = new MongoBook();
        mongo.setId(null);
        mongo.setTitle(pg.getTitle());
        mongo.setAuthorId(authorIdMongo);
        mongo.setGenresIds(genresIds);
        mongo.setVersion(pg.getVersion());
        return mongo;
    }

    private MongoComment mapComment(JpaComment pg) {
        JpaBook book = pg.getJpaBook();
        String authorFullName = book.getJpaAuthor().getFullName();
        String title = book.getTitle();

        String mongoBookId = findMongoBookIdOrThrow(authorFullName, title);

        MongoComment mongo = new MongoComment();
        mongo.setId(null);
        mongo.setBookId(mongoBookId);
        mongo.setText(pg.getText());
        mongo.setCreatedAt(pg.getCreatedAt());
        mongo.setVersion(pg.getVersion());
        return mongo;
    }

    private String findMongoAuthorIdOrThrow(String authorFullName) {
        return idMapping.findMongoAuthorId(authorFullName)
                .orElseThrow(() -> new MissingMappingException(
                        "No Mongo AuthorId for fullName=" + authorFullName +
                                ". Ensure authorsStep completed and mapping recorded."
                ));
    }

    private String findMongoGenreIdOrThrow(String genreName) {
        return idMapping.findMongoGenreId(genreName)
                .orElseThrow(() -> new MissingMappingException(
                        "No Mongo GenreId for name=" + genreName +
                                ". Ensure genresStep completed and mapping recorded."
                ));
    }

    private String findMongoBookIdOrThrow(String authorFullName, String title) {
        return idMapping.findMongoBookId(authorFullName, title)
                .orElseThrow(() -> new MissingMappingException(
                        "No Mongo BookId for (" + authorFullName + ", " + title + ")" +
                                ". Ensure booksStep completed and mapping recorded."
                ));
    }

    private List<String> mapGenresToIds(Collection<JpaGenre> jpaGenres) {
        List<String> ids = new ArrayList<>(jpaGenres.size());
        for (JpaGenre g : jpaGenres) {
            ids.add(findMongoGenreIdOrThrow(g.getName()));
        }
        return ids;
    }
}

