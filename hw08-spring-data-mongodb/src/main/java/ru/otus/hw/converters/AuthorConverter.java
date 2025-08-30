package ru.otus.hw.converters;

import org.springframework.stereotype.Component;
import ru.otus.hw.models.Author;

@Component
public class AuthorConverter {

    public String authorToString(Author author) {
        if (author == null) {
            return "";
        }
        String id = author.getId() == null ? "-" : author.getId();
        return "%s  %s".formatted(id, author.getFullName().trim());
    }

    public String headerWithCount(int count) {
        return "Authors (%d)".formatted(count);
    }
}
