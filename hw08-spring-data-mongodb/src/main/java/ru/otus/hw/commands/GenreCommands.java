package ru.otus.hw.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.hw.converters.GenreConverter;
import ru.otus.hw.services.GenreService;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@ShellComponent
public class GenreCommands {

    private final GenreService genreService;

    private final GenreConverter genreConverter;

    @ShellMethod(value = "Find all genres", key = "ag")
    public String findAllGenres() {
        var genres = genreService.findAll();
        var header = genreConverter.headerWithCount(genres.size());
        if (genres.isEmpty()) {
            return header + System.lineSeparator() + "— nothing to show —";
        }
        var body = genres.stream()
                .map(genreConverter::genreToString)
                .collect(Collectors.joining(System.lineSeparator()));
        return header + System.lineSeparator() + body;
    }
}
