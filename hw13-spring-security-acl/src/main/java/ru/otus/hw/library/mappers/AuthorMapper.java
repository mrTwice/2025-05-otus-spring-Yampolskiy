package ru.otus.hw.library.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.otus.hw.library.dto.AuthorDto;
import ru.otus.hw.library.models.Author;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface AuthorMapper {
    AuthorDto toDto(Author source);

    @Mapping(target = "version", ignore = true)
    Author toEntity(AuthorDto dto);
}