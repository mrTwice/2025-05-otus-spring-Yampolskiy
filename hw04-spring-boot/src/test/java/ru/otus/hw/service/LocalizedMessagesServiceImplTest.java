package ru.otus.hw.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class LocalizedMessagesServiceImplTest {

    @Autowired
    private LocalizedMessagesService messages;

    @MockitoBean
    private MessageSource messageSource;

    @Test
    void getMessage_shouldDelegateToMessageSourceWithLocaleFromLocaleConfig() {
        var ruRU = Locale.forLanguageTag("ru-RU");

        when(messageSource.getMessage(
                eq("welcome"),
                aryEq(new Object[]{"Иван"}),
                eq(ruRU))
        ).thenReturn("Добро пожаловать, Иван");

        String actual = messages.getMessage("welcome", "Иван");

        assertThat(actual).isEqualTo("Добро пожаловать, Иван");
        verify(messageSource).getMessage(eq("welcome"), aryEq(new Object[]{"Иван"}), eq(ruRU));
    }
}
