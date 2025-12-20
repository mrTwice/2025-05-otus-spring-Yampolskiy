package ru.otus.java.springframework.yampolskiy.mappers;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.otus.java.springframework.yampolskiy.dto.GenreDto;
import ru.otus.java.springframework.yampolskiy.models.Genre;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface GenreMapper {
    GenreDto toDto(Genre source);

    @Mapping(target = "version", ignore = true)
    Genre toEntity(GenreDto dto);
}