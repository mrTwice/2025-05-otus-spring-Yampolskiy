package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final IOService ioService;

    private final QuestionDao questionDao;

    @Override
    public TestResult executeTestFor(Student student) {
        ioService.printLine("");
        ioService.printLine("Please answer the questions below");

        var questions = questionDao.findAll();
        var result = new TestResult(student);

        for (var question : questions) {
            askAndGrade(question, result);
            ioService.printLine("");
        }
        return result;
    }

    private void askAndGrade(Question question, TestResult result) {
        ioService.printFormattedLine("%s", question.text());
        var answers = question.answers();
        showAnswers(answers);

        int chosenIndex = promptChoiceIndex(answers.size());
        boolean correct = isCorrectChoice(answers, chosenIndex);
        result.applyAnswer(question, correct);
    }

    private void showAnswers(List<Answer> answers) {
        for (int i = 0; i < answers.size(); i++) {
            ioService.printFormattedLine("%d) %s", i + 1, answers.get(i).text());
        }
    }

    private int promptChoiceIndex(int answersCount) {
        int choice1Based = ioService.readIntForRangeWithPrompt(
                1, answersCount,
                "Enter the option number:",
                "Invalid option. Try again."
        );
        return choice1Based - 1;
    }

    private boolean isCorrectChoice(List<Answer> answers, int index) {
        return answers.get(index).isCorrect();
    }
}

