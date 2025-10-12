package ru.otus.hw.migration.pg2mongo;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import ru.otus.hw.domain.mongo.MongoComment;

@RequiredArgsConstructor
public class CommentMongoUpsertWriter implements ItemWriter<MongoComment> {

    private final MongoTemplate mongoTemplate;

    @Override
    public void write(Chunk<? extends MongoComment> chunk) {
        for (MongoComment item : chunk) {
            Query q = Query.query(
                    Criteria.where("bookId").is(item.getBookId())
                            .and("createdAt").is(item.getCreatedAt())
                            .and("text").is(item.getText())
            );
            Update u = new Update()
                    .set("bookId", item.getBookId())
                    .set("createdAt", item.getCreatedAt())
                    .set("text", item.getText())
                    .set("version", item.getVersion());

            mongoTemplate.findAndModify(
                    q, u, FindAndModifyOptions.options().upsert(true).returnNew(true), MongoComment.class
            );
        }
    }
}
