package ru.otus.hw.migration.mongo2pg;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.data.MongoCursorItemReader;
import org.springframework.batch.item.data.builder.MongoCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import ru.otus.hw.domain.mongo.MongoAuthor;
import ru.otus.hw.domain.mongo.MongoBook;
import ru.otus.hw.domain.mongo.MongoComment;
import ru.otus.hw.domain.mongo.MongoGenre;

import java.util.LinkedHashMap;
import java.util.Map;


@Configuration
@RequiredArgsConstructor
public class MongoToPgReadersConfig {

    private final MongoTemplate mongoTemplate;

    @Value("${migration.chunk-size:500}")
    private int chunkSize;

    @Bean
    @StepScope
    public MongoCursorItemReader<MongoAuthor> authorsMongoReader() {
        Map<String, Sort.Direction> sort = Map.of("fullName", Sort.Direction.ASC, "_id", Sort.Direction.ASC);
        return new MongoCursorItemReaderBuilder<MongoAuthor>()
                .name("authorsMongoReader")
                .template(mongoTemplate)
                .targetType(MongoAuthor.class)
                .jsonQuery("{}")
                .sorts(sort)
                .batchSize(chunkSize)
                .build();
    }

    @Bean
    @StepScope
    public MongoCursorItemReader<MongoGenre> genresMongoReader() {
        Map<String, Sort.Direction> sort = Map.of("name", Sort.Direction.ASC, "_id", Sort.Direction.ASC);
        return new MongoCursorItemReaderBuilder<MongoGenre>()
                .name("genresMongoReader")
                .template(mongoTemplate)
                .targetType(MongoGenre.class)
                .jsonQuery("{}")
                .sorts(sort)
                .batchSize(chunkSize)
                .build();
    }

    @Bean
    @StepScope
    public MongoCursorItemReader<MongoBook> booksMongoReader() {
        Map<String, Sort.Direction> sort = new LinkedHashMap<>();
        sort.put("authorId", Sort.Direction.ASC);
        sort.put("title", Sort.Direction.ASC);
        sort.put("_id", Sort.Direction.ASC);

        return new MongoCursorItemReaderBuilder<MongoBook>()
                .name("booksMongoReader")
                .template(mongoTemplate)
                .targetType(MongoBook.class)
                .jsonQuery("{}")
                .sorts(sort)
                .batchSize(chunkSize)
                .build();
    }

    @Bean
    @StepScope
    public MongoCursorItemReader<MongoComment> commentsMongoReader(
            @Value("#{jobParameters['since']}") String since
    ) {
        String query;
        if (since != null && !since.isBlank()) {
            query = String.format("{\"createdAt\":{\"$gte\":{\"$date\":\"%s\"}}}", since);
        } else {
            query = "{}";
        }

        Map<String, Sort.Direction> sort = new LinkedHashMap<>();
        sort.put("bookId", Sort.Direction.ASC);
        sort.put("createdAt", Sort.Direction.ASC);
        sort.put("_id", Sort.Direction.ASC);

        return new MongoCursorItemReaderBuilder<MongoComment>()
                .name("commentsMongoReader")
                .template(mongoTemplate)
                .targetType(MongoComment.class)
                .jsonQuery(query)
                .sorts(sort)
                .batchSize(chunkSize)
                .build();
    }
}
