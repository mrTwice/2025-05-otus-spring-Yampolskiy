package ru.otus.hw.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.otus.hw.config.LocaleConfig;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.context.support.StaticMessageSource;

@ExtendWith(SpringExtension.class)
@Import({ LocalizedMessagesServiceImpl.class, LocalizedMessagesServicePrimaryContextTest.TestBeans.class })
class LocalizedMessagesServicePrimaryContextTest {

    @Autowired
    private LocalizedMessagesService service;

    @TestConfiguration(proxyBeanMethods = false)
    static class TestBeans {
        @Bean
        LocalizedMessagesService nonPrimaryMessagesService() {
            return (code, args) -> "NON_PRIMARY_" + code;
        }

        @Bean
        LocaleConfig localeConfig() {
            return () -> Locale.forLanguageTag("en-US");
        }

        @Bean
        MessageSource messageSource() {
            StaticMessageSource sms = new StaticMessageSource();
            sms.addMessage("greet", Locale.forLanguageTag("en-US"), "Hello, {0}");
            return sms;
        }
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
