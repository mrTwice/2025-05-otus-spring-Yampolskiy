package ru.otus.java.springframework.yampolskiy.mappers;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.otus.java.springframework.yampolskiy.dto.AuthorDto;
import ru.otus.java.springframework.yampolskiy.models.Author;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface AuthorMapper {
    AuthorDto toDto(Author source);

    @Mapping(target = "version", ignore = true)
    Author toEntity(AuthorDto dto);
}