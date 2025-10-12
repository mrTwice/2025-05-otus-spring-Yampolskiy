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
import ru.otus.hw.migration.cache.IdMappingService;


@RequiredArgsConstructor
public class AuthorMongoUpsertWriter implements ItemWriter<MongoAuthor> {

    private final MongoTemplate mongoTemplate;

    private final IdMappingService idMapping;

    @Override
    public void write(Chunk<? extends MongoAuthor> chunk) {
        for (MongoAuthor item : chunk) {
            Query q = Query.query(Criteria.where("fullName").is(item.getFullName()));
            Update u = new Update()
                    .set("fullName", item.getFullName())
                    .set("version", item.getVersion());

            MongoAuthor saved = mongoTemplate.findAndModify(
                    q, u, FindAndModifyOptions.options().upsert(true).returnNew(true), MongoAuthor.class
            );
            idMapping.rememberMongoAuthor(item.getFullName(), saved.getId());
        }
    }
}
