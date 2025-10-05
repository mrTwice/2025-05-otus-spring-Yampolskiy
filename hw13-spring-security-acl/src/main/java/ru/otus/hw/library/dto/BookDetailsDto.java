package ru.otus.hw.library.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookDetailsDto {
    private Long id;

    private String title;

    private AuthorDto author;

    private List<GenreDto> genres;

    private long version;
}