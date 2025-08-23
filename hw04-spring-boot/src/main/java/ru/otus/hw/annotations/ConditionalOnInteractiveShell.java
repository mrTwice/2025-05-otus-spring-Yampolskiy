package ru.otus.hw.annotations;

import org.jline.terminal.Terminal;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.shell.Shell;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;


@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnClass({ Shell.class, Terminal.class })
@ConditionalOnNotWebApplication
@ConditionalOnProperty(
        name = { "spring.shell.interactive.enabled", "app.shell.enabled" },
        havingValue = "true",
        matchIfMissing = true
)
@Profile("!test")
public @interface ConditionalOnInteractiveShell { }
