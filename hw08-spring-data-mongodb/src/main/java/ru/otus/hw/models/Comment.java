package ru.otus.hw.models;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Document(collection = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(name = "idx_comments_book_createdAt", def = "{'bookId': 1, 'createdAt': -1}")
public class Comment {

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
}