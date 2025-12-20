package ru.otus.hw.library.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.otus.hw.library.dto.GenreDto;
import ru.otus.hw.library.models.Genre;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface GenreMapper {
    GenreDto toDto(Genre source);

    @Mapping(target = "version", ignore = true)
    Genre toEntity(GenreDto dto);
}