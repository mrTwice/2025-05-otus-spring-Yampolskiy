package ru.otus.hw.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("LocalizedIOServiceImpl")
class LocalizedIOServiceImplTest {

    @Autowired
    private LocalizedIOService localizedIOService;

    @MockitoBean
    private IOService ioService;

    @MockitoBean
    private LocalizedMessagesService localizedMessagesService;

    @Test
    @DisplayName("делегирует printLine в IOService")
    void shouldDelegatePrintLine() {
        localizedIOService.printLine("Hello");
        verify(ioService).printLine("Hello");
    }

    @Test
    @DisplayName("делегирует printFormattedLine в IOService")
    void shouldDelegatePrintFormattedLine() {
        localizedIOService.printFormattedLine("Hello %s", "World");
        verify(ioService).printFormattedLine("Hello %s", "World");
    }

    @Test
    @DisplayName("делегирует readString в IOService")
    void shouldDelegateReadString() {
        when(ioService.readString()).thenReturn("test");
        String result = localizedIOService.readString();
        verify(ioService).readString();
        assertEquals("test", result);
    }

    @Test
    @DisplayName("использует локализованное сообщение для printLineLocalized")
    void shouldUseLocalizedMessageForPrintLineLocalized() {
        when(localizedMessagesService.getMessage("greeting")).thenReturn("Привет");
        localizedIOService.printLineLocalized("greeting");
        verify(ioService).printLine("Привет");
    }

    @Test
    @DisplayName("использует локализованное сообщение для printFormattedLineLocalized")
    void shouldUseLocalizedMessageForPrintFormattedLineLocalized() {
        when(localizedMessagesService.getMessage("welcome", "John"))
                .thenReturn("Добро пожаловать, John");
        localizedIOService.printFormattedLineLocalized("welcome", "John");
        verify(ioService).printLine("Добро пожаловать, John");
    }

    @Test
    @DisplayName("использует локализованный промпт для readStringWithPromptLocalized")
    void shouldUseLocalizedMessageForReadStringWithPromptLocalized() {
        when(localizedMessagesService.getMessage("enterName")).thenReturn("Введите имя");
        when(ioService.readStringWithPrompt("Введите имя")).thenReturn("Иван");

        String result = localizedIOService.readStringWithPromptLocalized("enterName");

        verify(ioService).readStringWithPrompt("Введите имя");
        assertEquals("Иван", result);
    }

    @Test
    @DisplayName("использует локализованные промпт/ошибку для readIntForRangeWithPromptLocalized")
    void shouldUseLocalizedMessagesForReadIntForRangeWithPromptLocalized() {
        when(localizedMessagesService.getMessage("prompt")).thenReturn("Введите число");
        when(localizedMessagesService.getMessage("error")).thenReturn("Ошибка ввода");
        when(ioService.readIntForRangeWithPrompt(1, 10, "Введите число", "Ошибка ввода"))
                .thenReturn(5);

        int result = localizedIOService.readIntForRangeWithPromptLocalized(1, 10, "prompt", "error");

        verify(ioService).readIntForRangeWithPrompt(1, 10, "Введите число", "Ошибка ввода");
        assertEquals(5, result);
    }
}
