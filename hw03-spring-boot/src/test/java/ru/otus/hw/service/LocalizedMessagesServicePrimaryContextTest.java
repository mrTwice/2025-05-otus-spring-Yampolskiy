package ru.otus.hw.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.Locale;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.hw.config.LocaleConfig;


@ExtendWith(SpringExtension.class)
@Import(LocalizedMessagesServiceImpl.class)
@ImportAutoConfiguration(MessageSourceAutoConfiguration.class)
@TestPropertySource(properties = {
        "spring.messages.basename=messages"
})
class LocalizedMessagesServicePrimaryContextTest {

    @Autowired
    LocalizedMessagesService service;

    @MockitoBean
    LocaleConfig localeConfig;

    @MockitoBean(name = "nonPrimaryMessagesService")
    LocalizedMessagesService nonPrimary;

    @BeforeEach
    void setUp() {
        given(localeConfig.getLocale()).willReturn(Locale.forLanguageTag("en-US"));
    }

    @Test
    void shouldInjectPrimaryImplementation() {
        assertThat(service).isInstanceOf(LocalizedMessagesServiceImpl.class);
    }

    @Test
    void shouldResolveMessageWithLocaleFromLocaleConfig() {
        String result = service.getMessage("greet", "John");
        assertThat(result).isEqualTo("Hello, John");
    }
}
