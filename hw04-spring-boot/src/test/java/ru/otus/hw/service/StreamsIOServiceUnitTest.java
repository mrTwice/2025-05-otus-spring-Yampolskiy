package ru.otus.hw.service;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StreamsIOServiceUnitTest {

    private StreamsIOService createService(String input, ByteArrayOutputStream outBuf) {
        InputStream in = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        PrintStream out = new PrintStream(outBuf, true, StandardCharsets.UTF_8);
        return new StreamsIOService(out, in);
    }

    @Test
    void printLine_writesLineWithNewline() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamsIOService svc = createService("", out);

        svc.printLine("Hello");
        assertThat(out.toString(StandardCharsets.UTF_8)).isEqualTo("Hello" + System.lineSeparator());
    }

    @Test
    void printFormattedLine_formatsAndAppendsNewline() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamsIOService svc = createService("", out);

        svc.printFormattedLine("Hi, %s!", "John");
        assertThat(out.toString(StandardCharsets.UTF_8)).isEqualTo("Hi, John!" + System.lineSeparator());
    }

    @Test
    void readString_readsSingleLine() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamsIOService svc = createService("abc\n", out);

        String s = svc.readString();
        assertThat(s).isEqualTo("abc");
    }

    @Test
    void readStringWithPrompt_printsPrompt_thenReadsLine() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamsIOService svc = createService("value\n", out);

        String s = svc.readStringWithPrompt("Enter:");
        assertThat(s).isEqualTo("value");
        assertThat(out.toString(StandardCharsets.UTF_8)).isEqualTo("Enter:" + System.lineSeparator());
    }

    @Test
    void readIntForRange_acceptsValidNumberWithinRange() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamsIOService svc = createService("5\n", out);

        int v = svc.readIntForRange(1, 10, "err");
        assertThat(v).isEqualTo(5);
        assertThat(out.toString(StandardCharsets.UTF_8)).isEmpty();
    }

    @Test
    void readIntForRange_repromptsOnInvalid_thenReturnsValid() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamsIOService svc = createService("oops\n0\n3\n", out);

        int v = svc.readIntForRange(1, 5, "ERR");
        assertThat(v).isEqualTo(3);

        String stdout = out.toString(StandardCharsets.UTF_8);
        String expected = "ERR" + System.lineSeparator() + "ERR" + System.lineSeparator();
        assertThat(stdout).isEqualTo(expected);
    }

    @Test
    void readIntForRange_throwsAfterMaxAttempts() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) sb.append("oops\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamsIOService svc = createService(sb.toString(), out);

        assertThatThrownBy(() -> svc.readIntForRange(1, 2, "ERR"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Error during reading int value");

        String expected = ("ERR" + System.lineSeparator()).repeat(10);
        assertThat(out.toString(StandardCharsets.UTF_8)).isEqualTo(expected);
    }

    @Test
    void readIntForRangeWithPrompt_printsPrompt_thenDelegatesToRangeValidation() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamsIOService svc = createService("100\n2\n", out);

        int v = svc.readIntForRangeWithPrompt(1, 10, "Enter number:", "ERR");
        assertThat(v).isEqualTo(2);

        String expected = "Enter number:" + System.lineSeparator()
                + "ERR" + System.lineSeparator();
        assertThat(out.toString(StandardCharsets.UTF_8)).isEqualTo(expected);
    }
}
