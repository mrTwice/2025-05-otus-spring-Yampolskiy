package ru.otus.hw.migration.pg2mongo;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import ru.otus.hw.domain.mongo.MongoGenre;
import ru.otus.hw.migration.cache.IdMappingService;

@RequiredArgsConstructor
public class GenreMongoUpsertWriter implements ItemWriter<MongoGenre> {

    private final MongoTemplate mongoTemplate;

    private final IdMappingService idMapping;

    @Override
    public void write(Chunk<? extends MongoGenre> chunk) {
        for (MongoGenre item : chunk) {
            Query q = Query.query(Criteria.where("name").is(item.getName()));
            Update u = new Update()
                    .set("name", item.getName())
                    .set("version", item.getVersion());

            MongoGenre saved = mongoTemplate.findAndModify(
                    q, u, FindAndModifyOptions.options().upsert(true).returnNew(true), MongoGenre.class
            );
            idMapping.rememberMongoGenre(item.getName(), saved.getId());
        }
    }
}
