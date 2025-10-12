package ru.otus.hw.domain.mongo;

import jakarta.validation.constraints.NotBlank;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "comments")
@CompoundIndex(name = "idx_comments_book_createdAt", def = "{'bookId': 1, 'createdAt': -1}")
public class MongoComment {

    @Id
    private String id;

    @NotBlank
    private String text;

    @Field("createdAt")
    @CreatedDate
    private Instant createdAt;

    @NotBlank
    @Indexed
    private String bookId;

    @Version
    private long version;
}
