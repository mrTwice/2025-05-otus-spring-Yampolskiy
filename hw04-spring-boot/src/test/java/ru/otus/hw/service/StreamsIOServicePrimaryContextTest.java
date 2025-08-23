package ru.otus.hw.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class StreamsIOServicePrimaryContextTest {

    @MockitoBean(name = "nonPrimaryIoService")
    IOService extraIoService;

    @Autowired
    private IOService ioService;

    @Test
    void shouldInjectPrimaryStreamsIOServiceWhenMultipleBeansPresent() {
        assertThat(ioService).isInstanceOf(StreamsIOService.class);
    }
}