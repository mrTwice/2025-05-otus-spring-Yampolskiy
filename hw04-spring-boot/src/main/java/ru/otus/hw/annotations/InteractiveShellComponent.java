package ru.otus.hw.annotations;

import org.springframework.shell.standard.ShellComponent;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ShellComponent
@ConditionalOnInteractiveShell
public @interface InteractiveShellComponent { }