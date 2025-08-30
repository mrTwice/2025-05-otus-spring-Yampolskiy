package ru.otus.hw.shell;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.hw.annotations.InteractiveShellComponent;

@InteractiveShellComponent
@RequiredArgsConstructor
@ConditionalOnBean(ExaminationShellCommands.class)
public class SystemShellCommands {

    private final ConfigurableApplicationContext context;

    @ShellMethod(key = {"exit", "quit", ":q"}, value = "Exit the application")
    public void exit() {
        int code = SpringApplication.exit(context, () -> 0);
        System.exit(code);
    }
}
