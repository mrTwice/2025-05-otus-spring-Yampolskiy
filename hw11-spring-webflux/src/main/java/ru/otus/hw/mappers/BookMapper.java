package ru.otus.hw.mappers;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import ru.otus.hw.dto.BookDetailsDto;
import ru.otus.hw.dto.BookForm;
import ru.otus.hw.dto.BookListItemDto;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import java.util.stream.Collectors;
import ru.otus.hw.models.Author;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        uses = { AuthorMapper.class, GenreMapper.class }
)
public interface BookMapper {


    @Mapping(target = "id",        source = "id")
    @Mapping(target = "title",     source = "title")
    @Mapping(target = "authorId",  source = "authorId")
    @Mapping(target = "genresIds", ignore = true)
    @Mapping(target = "version",   source = "version")
    Book fromForm(BookForm form);

    @Mapping(target = "title",     source = "title")
    @Mapping(target = "version",   source = "version")
    @Mapping(target = "authorId",  source = "authorId")
    @Mapping(target = "genresIds", ignore = true)
    void updateFromForm(BookForm form, @MappingTarget Book target);

    @AfterMapping
    default void copyGenresIdsFromForm(BookForm form, @MappingTarget Book target) {
        if (form.getGenresIds() != null) {
            target.setGenresIds(new ArrayList<>(form.getGenresIds()));
        } else {
            target.setGenresIds(new ArrayList<>());
        }
    }

    @Mapping(target = "id",      source = "book.id")
    @Mapping(target = "title",   source = "book.title")
    @Mapping(target = "author",  source = "author")
    @Mapping(target = "genres",  source = "genres")
    @Mapping(target = "version", source = "book.version")
    BookDetailsDto toDetailsDto(Book book, Author author, List<Genre> genres);

    @Mapping(target = "authorFullName", source = "authorFullName")
    @Mapping(target = "genresSummary",  source = "genres", qualifiedByName = "joinGenreNames")
    BookListItemDto toListItemDto(Book book, String authorFullName, List<Genre> genres);

    @Named("joinGenreNames")
    default String joinGenreNames(Collection<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return "";
        }
        return genres.stream().map(Genre::getName).collect(Collectors.joining(", "));
    }
}

