package ru.otus.hw.migration.pg2mongo;

import com.mongodb.client.model.FindOneAndUpdateOptions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class MongoUpsertOptions {
    static final FindOneAndUpdateOptions UPSERT_RETURN_NEW =
            new FindOneAndUpdateOptions().upsert(true).returnDocument(com.mongodb.client.model.ReturnDocument.AFTER);
}
