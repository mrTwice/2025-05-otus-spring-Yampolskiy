package ru.otus.hw.domain.mongo;

import jakarta.validation.constraints.NotBlank;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("authors")
public class MongoAuthor {
    @Id
    private String id;

    @NotBlank
    @Indexed(unique = true)
    private String fullName;

    @Version
    private long version;
}
