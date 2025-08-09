package ru.otus.hw.service;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;
import ru.otus.hw.exceptions.QuestionReadException;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class TestServiceImplTest {

    @Mock
    IOService ioService;
    @Mock QuestionDao questionDao;

    @InjectMocks TestServiceImpl service;

    @Captor
    ArgumentCaptor<Integer> minCap;
    @Captor ArgumentCaptor<Integer> maxCap;

    private final Student student = new Student("John", "Doe");

    static Stream<Arguments> scenarios() {
        var q1 = new Question("Is Java platform-independent?",
                List.of(new Answer("Yes", true), new Answer("No", false)));
        var q2 = new Question("2 + 2 = ?",
                List.of(new Answer("3", false), new Answer("4", true), new Answer("5", false)));
        var q3 = new Question("Only one?", List.of(new Answer("Yes", true)));

        return Stream.of(
                Arguments.of(List.of(q1, q2), List.of(1, 3), 1),
                Arguments.of(List.of(q1, q2), List.of(2, 2), 1),
                Arguments.of(List.of(q3), List.of(1), 1),
                Arguments.of(List.of(), List.of(), 0)
        );
    }

    @ParameterizedTest
    @MethodSource("scenarios")
    void executeTestFor_parametrized(List<Question> questions, List<Integer> choices1Based, int expectedRight) {

        assertEquals(questions.size(), choices1Based.size(),
                "choices1Based must match questions count");

        when(questionDao.findAll()).thenReturn(questions);

        if (!questions.isEmpty()) {
            var idx = new AtomicInteger(0);
            when(ioService.readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString()))
                    .thenAnswer(inv -> choices1Based.get(idx.getAndIncrement()));
        }

        TestResult result = service.executeTestFor(student);

        assertNotNull(result);
        assertEquals(student, result.getStudent());
        assertEquals(questions, result.getAnsweredQuestions());
        assertEquals(expectedRight, result.getRightAnswersCount());

        InOrder inOrder = inOrder(ioService);
        inOrder.verify(ioService).printLine("");
        inOrder.verify(ioService).printLine("Please answer the questions below");

        for (Question q : questions) {
            inOrder.verify(ioService).printFormattedLine("%s", q.text());
            var answers = q.answers();
            for (int j = 0; j < answers.size(); j++) {
                inOrder.verify(ioService).printFormattedLine("%d) %s", j + 1, answers.get(j).text());
            }
            inOrder.verify(ioService).readIntForRangeWithPrompt(
                    1, answers.size(), "Enter the option number:", "Invalid option. Try again.");
            inOrder.verify(ioService).printLine("");
        }

        verifyNoMoreInteractions(ioService);
    }

    @Test
    void executeTestFor_selectsSecondOption_whenIoReturns2() {
        var q = new Question("Pick one", List.of(new Answer("A", true), new Answer("B", false)));
        when(questionDao.findAll()).thenReturn(List.of(q));
        when(ioService.readIntForRangeWithPrompt(eq(1), eq(2), anyString(), anyString()))
                .thenReturn(2);

        TestResult result = service.executeTestFor(student);

        assertEquals(1, result.getAnsweredQuestions().size());
        assertEquals(0, result.getRightAnswersCount());
    }

    @Test
    void executeTestFor_noQuestions_noInput() {
        when(questionDao.findAll()).thenReturn(List.of());

        TestResult result = service.executeTestFor(student);

        assertNotNull(result);
        assertEquals(0, result.getAnsweredQuestions().size());
        assertEquals(0, result.getRightAnswersCount());
        verify(ioService, never()).readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString());
    }

    @Test
    void executeTestFor_propagatesDaoException() {
        when(questionDao.findAll()).thenThrow(new QuestionReadException("DB error"));
        assertThrows(QuestionReadException.class, () -> service.executeTestFor(student));
    }

    @Test
    void executeTestFor_propagatesIoException() {
        when(questionDao.findAll()).thenReturn(List.of(
                new Question("Q?", List.of(new Answer("A", true)))
        ));
        doThrow(new RuntimeException("IO fail"))
                .when(ioService).printLine("Please answer the questions below");

        assertThrows(RuntimeException.class, () -> service.executeTestFor(student));
    }

    @Test
    void executeTestFor_throws_whenQuestionHasNoAnswers() {
        var q = new Question("Empty?", List.of());
        when(questionDao.findAll()).thenReturn(List.of(q));

        assertThrows(RuntimeException.class, () -> service.executeTestFor(student));
    }

    @Test
    void executeTestFor_propagates_whenPrintingAnswerFails() {
        var q = new Question("Q", List.of(new Answer("A", true)));
        when(questionDao.findAll()).thenReturn(List.of(q));
        doThrow(new RuntimeException("IO fail"))
                .when(ioService).printFormattedLine("%d) %s", 1, "A");

        assertThrows(RuntimeException.class, () -> service.executeTestFor(student));

    }

    @ParameterizedTest
    @MethodSource("scenarios")
    void executeTestFor_promptsWithExactBounds_param(
            List<Question> questions,
            List<Integer> choices1Based,
            int ignoredExpectedRight
    ) {
        when(questionDao.findAll()).thenReturn(questions);

        if (!questions.isEmpty()) {
            var i = new AtomicInteger(0);
            when(ioService.readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString()))
                    .thenAnswer(inv -> choices1Based.get(i.getAndIncrement()));
        }

        service.executeTestFor(student);

        if (questions.isEmpty()) {
            verify(ioService, never()).readIntForRangeWithPrompt(anyInt(), anyInt(), anyString(), anyString());
            return;
        }

        int prompts = questions.size();
        verify(ioService, org.mockito.Mockito.times(prompts))
                .readIntForRangeWithPrompt(minCap.capture(), maxCap.capture(), anyString(), anyString());

        assertEquals(java.util.Collections.nCopies(prompts, 1), minCap.getAllValues());

        var expectedMax = questions.stream().map(q -> q.answers().size()).toList();
        assertEquals(expectedMax, maxCap.getAllValues());
    }
}

