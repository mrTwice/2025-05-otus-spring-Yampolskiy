package ru.otus.hw.migration.mongo2pg;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.otus.hw.migration.cache.IdMappingService;

@Configuration
@RequiredArgsConstructor
public class MongoToPgWritersConfig {

    private final IdMappingService idMapping;

    @Bean
    public AuthorPgUpsertWriter authorPgUpsertWriter() {
        return new AuthorPgUpsertWriter(idMapping);
    }

    @Bean
    public GenrePgUpsertWriter genrePgUpsertWriter() {
        return new GenrePgUpsertWriter(idMapping);
    }

    @Bean
    public BookPgUpsertWriter bookPgUpsertWriter() {
        return new BookPgUpsertWriter(idMapping);
    }

    @Bean
    public CommentPgUpsertWriter commentPgUpsertWriter() {
        return new CommentPgUpsertWriter();
    }
}
