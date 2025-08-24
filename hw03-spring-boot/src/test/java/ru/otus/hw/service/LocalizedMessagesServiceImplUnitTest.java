package ru.otus.hw.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;
import ru.otus.hw.config.LocaleConfig;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class LocalizedMessagesServiceImplUnitTest {

    @Test
    void getMessage_shouldDelegateToMessageSourceWithLocaleFromLocaleConfig() {
        LocaleConfig localeConfig = Mockito.mock(LocaleConfig.class);
        MessageSource messageSource = Mockito.mock(MessageSource.class);

        LocalizedMessagesServiceImpl sut = new LocalizedMessagesServiceImpl(localeConfig, messageSource);

        Locale ruRU = Locale.forLanguageTag("ru-RU");
        when(localeConfig.getLocale()).thenReturn(ruRU);
        when(messageSource.getMessage("welcome", new Object[]{"Иван"}, ruRU))
                .thenReturn("Добро пожаловать, Иван");

        String actual = sut.getMessage("welcome", "Иван");

        assertThat(actual).isEqualTo("Добро пожаловать, Иван");
        Mockito.verify(messageSource)
                .getMessage("welcome", new Object[]{"Иван"}, ruRU);
    }
}
