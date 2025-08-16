package ru.otus.hw.dao;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvQuestionDaoIT {

    private CsvQuestionDao newDao(String fileName) {
        ResourceLoader rl = new DefaultResourceLoader();
        TestFileNameProvider nameProvider = () -> fileName;
        return new CsvQuestionDao(nameProvider, rl);
    }

    @Test
    void findAll_readsRealCsvFromResources_andMapsToDomain() {
        CsvQuestionDao dao = newDao("it-questions.csv");

        List<Question> questions = dao.findAll();

        assertTrue(questions.size() >= 3, "ожидаем минимум 3 вопроса из тестового файла");

        Question q0 = questions.get(0);
        assertEquals("Is there life on Mars?", q0.text());
        assertEquals(3, q0.answers().size());

        Answer a00 = q0.answers().get(0);
        assertEquals("Science doesn't know this yet", a00.text());
        assertTrue(a00.isCorrect());

        Answer a01 = q0.answers().get(1);
        assertEquals("Certainly. The red UFO is from Mars. And green is from Venus", a01.text());
        assertFalse(a01.isCorrect());

        Answer a02 = q0.answers().get(2);
        assertEquals("Absolutely not", a02.text());
        assertFalse(a02.isCorrect());

        assertEquals("How should resources be loaded form jar in Java?", questions.get(1).text());
        assertEquals("Which keyword is used to inherit a class in Java?", questions.get(2).text());
    }

    @Test
    void findAll_throwsWhenResourceMissing() {
        CsvQuestionDao dao = newDao("no-such-file.csv");
        QuestionReadException ex = assertThrows(QuestionReadException.class, dao::findAll);
        assertTrue(ex.getMessage().contains("Resource not found"));
    }
}