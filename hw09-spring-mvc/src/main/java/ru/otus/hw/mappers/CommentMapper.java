package ru.otus.hw.mappers;

import org.mapstruct.Mapping;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.CommentForm;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface CommentMapper {

    @Mapping(target = "bookId", source = "book.id")
    CommentDto toDto(Comment entity);


    @Mapping(target = "id", source = "dto.id")
    @Mapping(target = "text", source = "dto.text")
    @Mapping(target = "createdAt", source = "dto.createdAt")
    @Mapping(target = "book", source = "book")
    @Mapping(target = "version", ignore = true)
    Comment fromDto(CommentDto dto, Book book);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "text", source = "form.text")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "book", source = "book")
    @Mapping(target = "version", ignore = true)
    Comment fromForm(CommentForm form, Book book);
}