package ru.otus.hw.models;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("authors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Author {
    @Id
    private String id;

    @NotBlank
    @Indexed(unique = true)
    private String fullName;

    @Version
    private long version;
}
