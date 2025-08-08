package ru.otus.hw.dao;

import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.dao.dto.QuestionDto;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;
import ru.otus.hw.utils.ResourceUtils;

import java.io.Reader;
import java.util.List;

@RequiredArgsConstructor
public class CsvQuestionDao implements QuestionDao {

    private final TestFileNameProvider fileNameProvider;

    @Override
    public List<Question> findAll() {
        String fileName = fileNameProvider.getTestFileName();
        Reader reader = ResourceUtils.getReaderFromResource(fileName);
        List<QuestionDto> dtos = parseCsv(reader);
        return mapToDomain(dtos);
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
