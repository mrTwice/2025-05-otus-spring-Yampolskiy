package ru.otus.hw.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookForm {

    private Long id;

    @NotBlank
    @Size(max = 255)
    private String title;

    @NotNull
    private Long authorId;

    @NotEmpty
    private Set<@NotNull Long> genresIds = new LinkedHashSet<>();

    @Builder.Default
    private long version = 0L;
}