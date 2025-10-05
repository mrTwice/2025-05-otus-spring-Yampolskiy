package ru.otus.hw.library.components;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ProblemDetailFactory {

    public ProblemDetail create(HttpStatus status, String title, String detail) {
        return create(status, title, detail, null);
    }

    public ProblemDetail create(HttpStatus status, String title, String detail, Map<String, ?> props) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        if (title != null) {
            pd.setTitle(title);
        }
        if (props != null) {
            props.forEach(pd::setProperty);
        }
        return pd;
    }
}