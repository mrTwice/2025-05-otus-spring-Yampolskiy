package ru.otus.hw.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.models.Author;

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