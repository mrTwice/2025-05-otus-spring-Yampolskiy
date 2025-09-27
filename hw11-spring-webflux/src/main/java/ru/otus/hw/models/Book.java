package ru.otus.hw.models;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document("books")
@CompoundIndex(name = "uniq_title_author", unique = true, def = "{'title':1,'authorId':1}")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
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

