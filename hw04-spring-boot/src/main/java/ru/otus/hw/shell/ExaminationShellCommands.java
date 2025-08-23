package ru.otus.hw.shell;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.hw.annotations.InteractiveShellComponent;
import ru.otus.hw.service.TestRunnerService;

@InteractiveShellComponent
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.shell.interactive.enabled", havingValue = "true")
public class ExaminationShellCommands {

    private final TestRunnerService testRunnerService;

    @ShellMethod(key = {"test", "run"}, value = "Run student testing and show the result")
    public void runTest() {
        testRunnerService.run();
    }
}
