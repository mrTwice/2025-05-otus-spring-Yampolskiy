package ru.otus.hw.migration.pg2mongo;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import ru.otus.hw.migration.cache.IdMappingService;
import ru.otus.hw.repo.mongo.MongoAuthorRepository;

@Configuration
@RequiredArgsConstructor
public class PgToMongoWritersConfig {

    private final MongoTemplate mongoTemplate;

    private final IdMappingService idMapping;

    private final MongoAuthorRepository authorMongoRepo;

    @Bean
    public AuthorMongoUpsertWriter authorMongoUpsertWriter() {
        return new AuthorMongoUpsertWriter(mongoTemplate, idMapping);
    }

    @Bean
    public GenreMongoUpsertWriter genreMongoUpsertWriter() {
        return new GenreMongoUpsertWriter(mongoTemplate, idMapping);
    }

    @Bean
    public BookMongoUpsertWriter bookMongoUpsertWriter() {
        return new BookMongoUpsertWriter(mongoTemplate, idMapping, authorMongoRepo);
    }

    @Bean
    public CommentMongoUpsertWriter commentMongoUpsertWriter() {
        return new CommentMongoUpsertWriter(mongoTemplate);
    }
}
