package ru.otus.hw.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestServiceImplTest {

    @Mock
    private IOService ioService;

    @Mock
    private QuestionDao questionDao;

    @InjectMocks
    private TestServiceImpl testService;

    @Captor
    private ArgumentCaptor<String> formatCaptor;

    @Captor
    private ArgumentCaptor<Object[]> argsCaptor;

    private static Stream<Arguments> questionSets() {
        return Stream.of(
                Arguments.of(
                        List.of(
                                new Question("Is Java platform-independent?", List.of(
                                        new Answer("Yes", true),
                                        new Answer("No", false)
                                )),
                                new Question("2 + 2 = ?", List.of(
                                        new Answer("4", true),
                                        new Answer("5", false)
                                ))
                        ),
                        List.of(
                                "%d. %s",   // "Is Java platform-independent?"
                                "   %d) %s", // "Yes"
                                "   %d) %s", // "No"
                                "%d. %s",   // "2 + 2 = ?"
                                "   %d) %s", // "4"
                                "   %d) %s"  // "5"
                        )
                ),
                Arguments.of(
                        List.of(
                                new Question("Only one?", List.of(
                                        new Answer("Yes", true)
                                ))
                        ),
                        List.of(
                                "%d. %s",
                                "   %d) %s"
                        )
                )
        );
    }


    @ParameterizedTest
    @MethodSource("questionSets")
    void executeTest_shouldPrintQuestions(List<Question> questions) {
        when(questionDao.findAll()).thenReturn(questions);

        testService.executeTest();

        verify(ioService, atLeast(1)).printFormattedLine(formatCaptor.capture(), argsCaptor.capture());

        long countQuestionHeaders = formatCaptor.getAllValues().stream()
                .filter(fmt -> fmt.equals("%d. %s"))
                .count();

        assertEquals(questions.size(), countQuestionHeaders);

        List<String> questionTexts = questions.stream().map(Question::text).toList();
        List<String> actualTexts = argsCaptor.getAllValues().stream()
                .filter(args -> args.length == 2 && args[0] instanceof Integer && args[1] instanceof String)
                .map(args -> (String) args[1])
                .toList();

        assertTrue(actualTexts.containsAll(questionTexts));
    }


    @Test
    void executeTest_shouldThrow_whenQuestionDaoFails() {
        when(questionDao.findAll()).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> testService.executeTest());
    }

    @Test
    void executeTest_shouldThrow_whenDaoReturnsNull() {
        when(questionDao.findAll()).thenReturn(null);

        assertThrows(NullPointerException.class, () -> testService.executeTest());
    }

    @Test
    void executeTest_shouldWork_whenNoQuestions() {
        when(questionDao.findAll()).thenReturn(List.of());

        testService.executeTest();

        verify(ioService).printFormattedLine("Please answer the questions below%n");
        verify(ioService, never()).printFormattedLine(startsWith("%d."), any());
    }

    @Test
    void executeTest_shouldFail_whenIoServiceFails() {
        when(questionDao.findAll()).thenReturn(List.of(
                new Question("Q?", List.of(new Answer("A", true)))
        ));

        doThrow(new RuntimeException("IO fail"))
                .when(ioService).printFormattedLine("Please answer the questions below");

        assertThrows(RuntimeException.class, () -> testService.executeTest());
    }
}
