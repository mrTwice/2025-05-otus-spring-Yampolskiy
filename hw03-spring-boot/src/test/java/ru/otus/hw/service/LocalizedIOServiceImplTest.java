package ru.otus.hw.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LocalizedIOServiceImplTest {

    private IOService ioService;
    private LocalizedMessagesService localizedMessagesService;
    private LocalizedIOServiceImpl localizedIOService;

    @BeforeEach
    void setUp() {
        ioService = mock(IOService.class);
        localizedMessagesService = mock(LocalizedMessagesService.class);
        localizedIOService = new LocalizedIOServiceImpl(localizedMessagesService, ioService);
    }

    @Test
    void shouldDelegatePrintLine() {
        localizedIOService.printLine("Hello");
        verify(ioService).printLine("Hello");
    }

    @Test
    void shouldDelegatePrintFormattedLine() {
        localizedIOService.printFormattedLine("Hello %s", "World");
        verify(ioService).printFormattedLine("Hello %s", "World");
    }

    @Test
    void shouldDelegateReadString() {
        when(ioService.readString()).thenReturn("test");
        String result = localizedIOService.readString();
        verify(ioService).readString();
        assert result.equals("test");
    }

    @Test
    void shouldUseLocalizedMessageForPrintLineLocalized() {
        when(localizedMessagesService.getMessage("greeting"))
                .thenReturn("Привет");
        localizedIOService.printLineLocalized("greeting");
        verify(ioService).printLine("Привет");
    }

    @Test
    void shouldUseLocalizedMessageForPrintFormattedLineLocalized() {
        when(localizedMessagesService.getMessage("welcome", "John"))
                .thenReturn("Добро пожаловать, John");
        localizedIOService.printFormattedLineLocalized("welcome", "John");
        verify(ioService).printLine("Добро пожаловать, John");
    }

    @Test
    void shouldUseLocalizedMessageForReadStringWithPromptLocalized() {
        when(localizedMessagesService.getMessage("enterName"))
                .thenReturn("Введите имя");
        when(ioService.readStringWithPrompt("Введите имя"))
                .thenReturn("Иван");

        String result = localizedIOService.readStringWithPromptLocalized("enterName");

        verify(ioService).readStringWithPrompt("Введите имя");
        assert result.equals("Иван");
    }

    @Test
    void shouldUseLocalizedMessagesForReadIntForRangeWithPromptLocalized() {
        when(localizedMessagesService.getMessage("prompt")).thenReturn("Введите число");
        when(localizedMessagesService.getMessage("error")).thenReturn("Ошибка ввода");

        when(ioService.readIntForRangeWithPrompt(1, 10, "Введите число", "Ошибка ввода"))
                .thenReturn(5);

        int result = localizedIOService.readIntForRangeWithPromptLocalized(1, 10, "prompt", "error");

        verify(ioService).readIntForRangeWithPrompt(1, 10, "Введите число", "Ошибка ввода");
        assert result == 5;
    }
}
