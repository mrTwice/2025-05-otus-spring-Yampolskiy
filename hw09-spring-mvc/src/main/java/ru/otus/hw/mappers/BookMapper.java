package ru.otus.hw.mappers;


import org.mapstruct.*;
import ru.otus.hw.components.AuthorRefResolver;
import ru.otus.hw.components.GenreRefResolver;
import ru.otus.hw.dto.BookDetailsDto;
import ru.otus.hw.dto.BookForm;
import ru.otus.hw.dto.BookListItemDto;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.Set;
import java.util.stream.Collectors;


@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        uses = {AuthorMapper.class, GenreMapper.class, AuthorRefResolver.class, GenreRefResolver.class}
)
public interface BookMapper {

    BookDetailsDto toDetailsDto(Book entity);

    @Mappings({
            @Mapping(target = "authorFullName", source = "author.fullName"),
            @Mapping(target = "genresSummary", source = "entity", qualifiedByName = "joinGenreNames")
    })
    BookListItemDto toListItemDto(Book entity);

    @Mappings({
            @Mapping(target = "author", source = "authorId", qualifiedByName = "resolveAuthor"),
            @Mapping(target = "genres", ignore = true)
    })
    Book fromForm(BookForm form, @Context AuthorRefResolver authorRef, @Context GenreRefResolver genreRef);

    @Mappings({
            @Mapping(target = "author", source = "authorId", qualifiedByName = "resolveAuthor"),
            @Mapping(target = "version", source = "version"),
            @Mapping(target = "title", source = "title"),
            @Mapping(target = "genres", ignore = true)
    })
    void updateFromForm(BookForm form, @MappingTarget Book target,
                        @Context AuthorRefResolver authorRef, @Context GenreRefResolver genreRef);

    @AfterMapping
    default void applyGenres(BookForm form, @MappingTarget Book target, @Context GenreRefResolver genreRef) {
        Set<Genre> resolved = resolveGenres(form.getGenresIds(), genreRef);
        target.replaceGenres(resolved);
    }

    @Named("resolveAuthor")
    default Author resolveAuthor(Long id, @Context AuthorRefResolver resolver) {
        return resolver.byId(id);
    }

    @Named("resolveGenres")
    default Set<Genre> resolveGenres(Set<Long> ids, @Context GenreRefResolver resolver) {
        return resolver.byIds(ids);
    }

    @Named("joinGenreNames")
    default String joinGenreNames(Book src) {
        if (src.getGenres() == null || src.getGenres().isEmpty()) return "";
        return src.getGenres().stream()
                .map(Genre::getName)
                .collect(Collectors.joining(", "));
    }
}
