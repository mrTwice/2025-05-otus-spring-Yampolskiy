package ru.otus.java.springframework.yampolskiy.mappers;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.otus.java.springframework.yampolskiy.dto.CommentDto;
import ru.otus.java.springframework.yampolskiy.dto.CommentForm;
import ru.otus.java.springframework.yampolskiy.models.Book;
import ru.otus.java.springframework.yampolskiy.models.Comment;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
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