package ru.otus.hw.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@Import({StreamsIOService.class, StreamsIOServicePrimaryContextTest.TestBeans.class})
class StreamsIOServicePrimaryContextTest {

    @TestConfiguration(proxyBeanMethods = false)
    static class TestBeans {
        @Bean
        IOService nonPrimaryIoService() {
            return new IOService() {
                @Override public void printLine(String s) {}
                @Override public void printFormattedLine(String s, Object... args) {}
                @Override public String readString() { return "stub"; }
                @Override public String readStringWithPrompt(String prompt) { return "stub"; }
                @Override public int readIntForRange(int min, int max, String errorMessage) { return min; }
                @Override public int readIntForRangeWithPrompt(int min, int max, String prompt, String errorMessage) { return min; }
            };
        }
    }

    @Autowired
    private IOService ioService;

    @Test
    void shouldInjectPrimaryStreamsIOServiceWhenMultipleBeansPresent() {
        assertThat(ioService).isInstanceOf(StreamsIOService.class);
    }
}
