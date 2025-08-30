package ru.otus.hw.components;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.ResultHandler;
import org.springframework.stereotype.Component;
import ru.otus.hw.exceptions.ConflictException;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.exceptions.ValidationException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ShellExceptionHandler implements ResultHandler<Throwable> {

    @Override
    public void handleResult(Throwable t) {
        Throwable e = unwrap(t);

        if (e instanceof ValidationException
                || e instanceof EntityNotFoundException
                || e instanceof ConflictException
                || e instanceof IllegalArgumentException) {
            System.out.println("Ошибка: " + safeMsg(e.getMessage()));
            return;
        }

        String msg = safeMsg(e.getMessage());
        if (msg.startsWith("No command found")) {
            System.out.println("Нет такой команды. " + msg);
            System.out.println("Подсказка: введите 'help' для списка команд.");
            return;
        }

        System.out.printf("Неожиданная ошибка: %s — %s%n",
                e.getClass().getSimpleName(), msg);
    }

    private static String safeMsg(String m) {
        return m == null ? "" : m;
    }

    private static Throwable unwrap(Throwable t) {
        Throwable e = t;
        while (true) {
            if (e instanceof InvocationTargetException ite && ite.getTargetException() != null) {
                e = ite.getTargetException();
                continue;
            }
            if (e instanceof UndeclaredThrowableException ute && ute.getUndeclaredThrowable() != null) {
                e = ute.getUndeclaredThrowable();
                continue;
            }
            break;
        }
        return e;
    }
}
