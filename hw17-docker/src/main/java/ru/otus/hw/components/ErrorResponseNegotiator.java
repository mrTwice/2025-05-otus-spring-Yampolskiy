package ru.otus.hw.components;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.Objects;

@Component
public class ErrorResponseNegotiator {

    private final ProblemDetailFactory pdf;

    public ErrorResponseNegotiator(ProblemDetailFactory pdf) {
        this.pdf = pdf;
    }

    public boolean wantsJson(HttpServletRequest req) {
        String accept = req.getHeader("Accept");
        String xrw = req.getHeader("X-Requested-With");
        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(xrw);
        boolean acceptsJson = accept != null && (
                accept.contains("application/json")
                        || accept.contains("application/problem+json")
        );
        return isAjax || acceptsJson;
    }

    public Object respond(HttpServletRequest req, ErrorResponseParams params) {
        HttpStatus status = Objects.requireNonNullElse(params.status(), HttpStatus.INTERNAL_SERVER_ERROR);
        String title = Objects.toString(params.title(), "");
        String detail = Objects.toString(params.detail(), "");
        Map<String, ?> props = (params.props() == null) ? Map.of() : params.props();

        if (wantsJson(req)) {
            return ResponseEntity.status(status)
                    .body(pdf.create(status, title, detail, props));
        }

        String view = (params.viewNameOrNull() != null) ? params.viewNameOrNull() : ("error/" + status.value());

        ModelAndView mav = new ModelAndView(view);
        mav.setStatus(status);
        mav.addObject("title", title);
        mav.addObject("message", detail);
        props.forEach((k, v) -> {
            if (!"title".equals(k) && !"message".equals(k)) {
                mav.addObject(k, v);
            }
        });
        return mav;
    }


    public Object respondJson(HttpServletRequest req, HttpStatus status, String title, String detail) {
        return respond(req, ErrorResponseParams.builder(status).title(title).detail(detail).build());
    }

    public Object respondView(HttpServletRequest req, HttpStatus status, String view, String detail) {
        return respond(req, ErrorResponseParams.builder(status).view(view).detail(detail).build());
    }
}
