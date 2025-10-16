package ru.otus.hw.domain.mongo;

import jakarta.validation.constraints.NotBlank;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("books")
@CompoundIndex(name = "uniq_title_author", def = "{'title':1,'authorId':1}", unique = true)
public class MongoBook {
    @Id
    private String id;

    @NotBlank
    private String title;

    @NotBlank
    @Indexed
    private String authorId;

    @Indexed
    private List<String> genresIds = new ArrayList<>();

    @Version
    private long version;
}
