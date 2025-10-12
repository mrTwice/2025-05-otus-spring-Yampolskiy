package ru.otus.hw.migration.pg2mongo;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import ru.otus.hw.domain.mongo.MongoAuthor;
import ru.otus.hw.domain.mongo.MongoBook;
import ru.otus.hw.migration.cache.IdMappingService;
import ru.otus.hw.repo.mongo.MongoAuthorRepository;

import java.util.ArrayList;

@RequiredArgsConstructor
public class BookMongoUpsertWriter implements ItemWriter<MongoBook> {

    private final MongoTemplate mongoTemplate;

    private final IdMappingService idMapping;

    private final MongoAuthorRepository authorMongoRepo;

    @Override
    public void write(Chunk<? extends MongoBook> chunk) {
        for (MongoBook item : chunk) {
            Query q = Query.query(
                    Criteria.where("title").is(item.getTitle())
                            .and("authorId").is(item.getAuthorId())
            );
            Update u = new Update()
                    .set("title", item.getTitle())
                    .set("authorId", item.getAuthorId())
                    .set("genresIds", new ArrayList<>(item.getGenresIds()))
                    .set("version", item.getVersion());

            MongoBook saved = mongoTemplate.findAndModify(
                    q, u, FindAndModifyOptions.options().upsert(true).returnNew(true), MongoBook.class
            );

            String authorFullName = authorMongoRepo.findById(item.getAuthorId())
                    .map(MongoAuthor::getFullName)
                    .orElse(null);
            if (authorFullName != null) {
                idMapping.rememberMongoBook(authorFullName, item.getTitle(), saved.getId());
            }
        }
    }
}
