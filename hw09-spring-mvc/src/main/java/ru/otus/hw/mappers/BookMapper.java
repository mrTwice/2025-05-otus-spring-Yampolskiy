package ru.otus.hw.mappers;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;
import ru.otus.hw.dto.BookDetailsDto;
import ru.otus.hw.dto.BookForm;
import ru.otus.hw.dto.BookListItemDto;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;


import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        uses = { AuthorMapper.class, GenreMapper.class }
)
public interface BookMapper {


    @Mappings({
            @Mapping(target = "authorFullName", source = "author.fullName"),
            @Mapping(target = "genresSummary", source = "genres", qualifiedByName = "joinGenreNames")
    })
    BookListItemDto toListItemDto(Book source);

    List<BookListItemDto> toListItemDtos(List<Book> source);

    default Page<BookListItemDto> toListItemPage(Page<Book> source) {
        return source.map(this::toListItemDto);
    }

    @Mappings({
            @Mapping(target = "author", source = "author"),
            @Mapping(target = "genres", source = "genres")
    })
    BookDetailsDto toDetailsDto(Book source);


    @Mappings({
            @Mapping(target = "authorId", source = "author.id"),
            @Mapping(target = "genresIds", source = "genres", qualifiedByName = "toIdSet")
    })
    BookForm toForm(Book source);


    @Named("joinGenreNames")
    default String joinGenreNames(Collection<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return "";
        }
        return genres.stream()
                .map(Genre::getName)
                .collect(Collectors.joining(", "));
    }

    @Named("toIdSet")
    default Set<Long> toIdSet(Collection<Genre> genres) {
        if (genres == null) {
            return Set.of();
        }
        return genres.stream()
                .map(Genre::getId)
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
    }
}
