package ru.otus.hw.dao;

import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.dao.dto.QuestionDto;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RequiredArgsConstructor
@Component
public class CsvQuestionDao implements QuestionDao {

    private final TestFileNameProvider fileNameProvider;

    private final ResourceLoader resourceLoader;

    @Override
    public List<Question> findAll() {
        String fileName = fileNameProvider.getTestFileName();
        Resource r = resourceLoader.getResource("classpath:" + fileName);
        if (!r.exists()) {
            throw new QuestionReadException("Resource not found: " + fileName);
        }
        try (Reader reader = new InputStreamReader(r.getInputStream(), StandardCharsets.UTF_8)) {
            return mapToDomain(parseCsv(reader));
        } catch (IOException e) {
            throw new QuestionReadException("Failed to read " + fileName, e);
        }
    }

    private List<QuestionDto> parseCsv(Reader reader) {
        try (reader) {
            return new CsvToBeanBuilder<QuestionDto>(reader)
                    .withType(QuestionDto.class)
                    .withSkipLines(1)
                    .withSeparator(';')
                    .build()
                    .parse();
        } catch (Exception e) {
            throw new QuestionReadException("Failed to read questions", e);
        }
    }

    private List<Question> mapToDomain(List<QuestionDto> dtos) {
        return dtos.stream()
                .map(QuestionDto::toDomainObject)
                .toList();
    }
}
