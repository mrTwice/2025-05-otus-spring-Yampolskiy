package ru.otus.hw.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "spring.messages.basename=messages",
        "test.locale=en-US"
})
@ActiveProfiles("test")
@DisplayName("LocalizedMessagesService (full context)")
class LocalizedMessagesServicePrimaryContextTest {

    @Autowired
    LocalizedMessagesService service;

    @Test
    @DisplayName("внедряется основная реализация")
    void shouldInjectPrimaryImplementation() {
        assertThat(service).isInstanceOf(LocalizedMessagesServiceImpl.class);
    }

    @Test
    @DisplayName("резолвит сообщение c локалью из LocaleConfig")
    void shouldResolveMessageWithLocaleFromLocaleConfig() {
        String result = service.getMessage("greet", "John");
        assertThat(result).isEqualTo("Hello, John");
    }
}
