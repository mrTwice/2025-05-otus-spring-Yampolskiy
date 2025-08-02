package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;

import java.util.List;

@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final IOService ioService;

    private final QuestionDao questionDao;

    @Override
    public void executeTest() {
        printHeader();
        List<Question> questions = questionDao.findAll();
        printQuestions(questions);
    }

    private void printHeader() {
        ioService.printFormattedLine("Please answer the questions below%n");
    }

    private void printQuestions(List<Question> questions) {
        int questionIndex = 1;
        for (Question question : questions) {
            printQuestion(questionIndex++, question);
            ioService.printLine("");
        }
    }

    private void printQuestion(int index, Question question) {
        ioService.printFormattedLine("%d. %s", index, question.text());

        int optionIndex = 1;
        for (Answer answer : question.answers()) {
            ioService.printFormattedLine("   %d) %s", optionIndex++, answer.text());
        }
    }
}
