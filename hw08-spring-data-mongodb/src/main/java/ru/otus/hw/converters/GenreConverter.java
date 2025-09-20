package ru.otus.hw.converters;

import org.springframework.stereotype.Component;
import ru.otus.hw.models.Genre;

@Component
public class GenreConverter {
    public String genreToString(Genre genre) {
        if (genre == null) {
            return "";
        }
        String id = genre.getId() == null ? "-" : genre.getId();
        String name = genre.getName().trim();
        return "%s  %s".formatted(id, name);
    }

    public String headerWithCount(int count) {
        return "Genres (%d)".formatted(count);
    }
}
