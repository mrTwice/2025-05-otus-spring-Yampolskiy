package ru.otus.hw.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.BDDMockito.given;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.hw.components.ErrorCodeHttpStatusMapper;
import ru.otus.hw.components.ProblemDetailFactory;
import ru.otus.hw.components.ValidationErrorExtractor;
import ru.otus.hw.exceptions.ErrorCode;

public class CommonTest {

    @MockitoBean
    private ErrorCodeHttpStatusMapper errorCodeHttpStatusMapper;

    @MockitoBean
    private ProblemDetailFactory problemDetailFactory;

    @MockitoBean
    private ValidationErrorExtractor validationErrorExtractor;

    protected void stubProblemDetailFactory() {
        given(problemDetailFactory.create(any(HttpStatus.class), anyString(), anyString(), anyMap()))
                .willAnswer(inv -> {
                    HttpStatus st = inv.getArgument(0);
                    String title = inv.getArgument(1);
                    String detail = inv.getArgument(2);
                    ProblemDetail pd = ProblemDetail.forStatus(st);
                    pd.setTitle(title);
                    pd.setDetail(detail);
                    return pd;
                });
        given(problemDetailFactory.create(any(HttpStatus.class), anyString(), anyString()))
                .willAnswer(inv -> {
                    HttpStatus st = inv.getArgument(0);
                    String title = inv.getArgument(1);
                    String detail = inv.getArgument(2);
                    ProblemDetail pd = ProblemDetail.forStatus(st);
                    pd.setTitle(title);
                    pd.setDetail(detail);
                    return pd;
                });
    }

    protected void stubStatusMapper() {
        given(errorCodeHttpStatusMapper.toStatus(ErrorCode.NOT_FOUND)).willReturn(HttpStatus.NOT_FOUND);
        given(errorCodeHttpStatusMapper.toStatus(ErrorCode.VALIDATION)).willReturn(HttpStatus.BAD_REQUEST);
        given(errorCodeHttpStatusMapper.toStatus(ErrorCode.CONFLICT)).willReturn(HttpStatus.CONFLICT);
        given(errorCodeHttpStatusMapper.toStatus(ErrorCode.DUPLICATE)).willReturn(HttpStatus.CONFLICT);
    }
}
