package ru.otus.hw.library.mappers;

import org.mapstruct.Mapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;
import ru.otus.hw.library.dto.CommentDto;
import ru.otus.hw.library.dto.CommentForm;
import ru.otus.hw.library.models.Book;
import ru.otus.hw.library.models.Comment;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface CommentMapper {

    @Mappings({
            @Mapping(target = "bookId", source = "book.id"),
            @Mapping(target = "authorId", source = "author.id"),
            @Mapping(target = "authorUsername", source = "author.username")
    })
    CommentDto toDto(Comment entity);


    @Mapping(target = "id", source = "dto.id")
    @Mapping(target = "text", source = "dto.text")
    @Mapping(target = "createdAt", source = "dto.createdAt")
    @Mapping(target = "book", source = "book")
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "version", ignore = true)
    Comment fromDto(CommentDto dto, Book book);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "text", source = "form.text")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "book", source = "book")
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "version", ignore = true)
    Comment fromForm(CommentForm form, Book book);
}